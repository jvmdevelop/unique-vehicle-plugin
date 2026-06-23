/**
 * UniqueVehiclePlugin — Vehicle Config Exporter for Blockbench
 *
 * HOW TO SET UP YOUR MODEL
 * ─────────────────────────────────────────────────────────────────────────
 * 1. Model origin (0, 0, 0) = vehicle spawn point (player's feet).
 *    Blockbench units: 16 px = 1 Minecraft block.
 *
 * 2. Create top-level groups with EXACT names:
 *      frame          — car body
 *      steering_wheel — steering column
 *      door_0         — left door
 *      door_1         — right door
 *      wheel_0        — front wheel (place at FRONT-LEFT axle)
 *      wheel_1        — rear wheel  (place at REAR-LEFT axle)
 *                       If wheel_1 is missing, wheel_0 is reused for rear.
 *      seat           — player seat position
 *
 * 3. For rotating parts (wheels, doors, steering_wheel):
 *    Set the Pivot Point of ANY cube inside the group to the rotation centre
 *    (axle, hinge, steering joint).  All cubes in the group must share the same
 *    Pivot Point — Blockbench sets this automatically when you use
 *    "Edit > Set Pivot" or drag the pivot handle.
 *
 *    The plugin reads:
 *      • group.origin / 16                → entity placement offset
 *      • (first_cube.pivot - group.origin) / 16  → rotation pivot in model space
 *
 * 4. wheel_0 generates BOTH front-left and front-right entries.
 *    Front-right X is mirrored: x_right = -x_left.
 *    Same for wheel_1 → rear-left and rear-right.
 *
 * 5. Run  File → Export → Export Vehicle Config
 *    Fill in the dialog and copy the YAML into config.yml.
 * ─────────────────────────────────────────────────────────────────────────
 */

(function () {
    "use strict";

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Find a top-level Group by name (case-insensitive). */
    function findGroup(name) {
        return Group.all.find(g => g.name.toLowerCase() === name.toLowerCase()) || null;
    }

    /** Format a number to 4 decimal places. */
    function fmt(n) { return parseFloat(n.toFixed(4)); }

    /** Blockbench pixels → Minecraft blocks. */
    function px(n) { return fmt(n / 16); }

    /**
     * Find the first Cube directly inside a group and return its pivot
     * (element.origin in Blockbench API = "Pivot Point" in the Transform panel).
     * Returns null if no cube is found.
     */
    function getFirstCubePivot(group) {
        for (const child of group.children) {
            if (child instanceof Cube) return child.origin; // [x, y, z] absolute px
        }
        // recurse one level into child groups
        for (const child of group.children) {
            if (child instanceof Group) {
                const p = getFirstCubePivot(child);
                if (p) return p;
            }
        }
        return null;
    }

    /**
     * Extract offset and pivot for a named group.
     *   offset = group.origin / 16  (entity placement from vehicle centre)
     *   pivot  = (cube.pivotPoint - group.origin) / 16  (rotation centre in model space)
     * Falls back to pivot (0,0,0) if no cube is found.
     */
    function extractPart(groupName) {
        const group = findGroup(groupName);
        if (!group) return null;

        const [gx, gy, gz] = group.origin;
        const offset = { x: px(gx), y: px(gy), z: px(gz) };

        let pivot = { x: 0, y: 0, z: 0 };
        const cp = getFirstCubePivot(group);
        if (cp) {
            pivot = {
                x: fmt((cp[0] - gx) / 16),
                y: fmt((cp[1] - gy) / 16),
                z: fmt((cp[2] - gz) / 16),
            };
        }

        return { offset, pivot };
    }

    // ── YAML generation ────────────────────────────────────────────────────

    function vec3yaml(v, indent) {
        return `${indent}x: ${v.x}\n${indent}y: ${v.y}\n${indent}z: ${v.z}`;
    }

    function generateYaml(vehicleId, partData, wheelCmd, doorCmds, frameCmd, swCmd) {
        const lines = [];
        const I2 = "    ";

        lines.push(`vehicles:`);
        lines.push(`  ${vehicleId}:`);

        // frame
        const frame = partData.frame;
        if (frame) {
            lines.push(`${I2}frame:`);
            lines.push(`${I2}  material: PAPER`);
            lines.push(`${I2}  custom-model-data: ${frameCmd}`);
            lines.push(`${I2}  offset:`);
            lines.push(vec3yaml(frame.offset, `${I2}    `));
        }

        // steering-wheel
        const sw = partData.steering_wheel;
        if (sw) {
            lines.push(`${I2}steering-wheel:`);
            lines.push(`${I2}  material: PAPER`);
            lines.push(`${I2}  custom-model-data: ${swCmd}`);
            lines.push(`${I2}  offset:`);
            lines.push(vec3yaml(sw.offset, `${I2}    `));
            lines.push(`${I2}  pivot:`);
            lines.push(vec3yaml(sw.pivot, `${I2}    `));
        }

        // doors
        lines.push(`${I2}doors:`);
        const doorTypes = ["LEFT", "RIGHT"];
        for (let i = 0; i < 2; i++) {
            const d = partData[`door_${i}`];
            if (!d) continue;
            lines.push(`${I2}  - type: ${doorTypes[i]}`);
            lines.push(`${I2}    material: PAPER`);
            lines.push(`${I2}    custom-model-data: ${doorCmds[i]}`);
            lines.push(`${I2}    offset:`);
            lines.push(vec3yaml(d.offset, `${I2}      `));
            lines.push(`${I2}    pivot:`);
            lines.push(vec3yaml(d.pivot, `${I2}      `));
        }

        // wheels — wheel_0 → FL + FR,  wheel_1 (or wheel_0) → RL + RR
        lines.push(`${I2}wheels:`);
        const frontData = partData.wheel_0;
        const rearData  = partData.wheel_1 || partData.wheel_0;

        const pairs = [
            { data: frontData, front: true  },
            { data: rearData,  front: false },
        ];

        for (const { data, front } of pairs) {
            if (!data) continue;
            // LEFT — original position
            lines.push(`${I2}  - type: LEFT`);
            lines.push(`${I2}    front: ${front}`);
            lines.push(`${I2}    material: PAPER`);
            lines.push(`${I2}    custom-model-data: ${wheelCmd}`);
            lines.push(`${I2}    offset:`);
            lines.push(vec3yaml(data.offset, `${I2}      `));
            lines.push(`${I2}    pivot:`);
            lines.push(vec3yaml(data.pivot, `${I2}      `));
            // RIGHT — mirror X
            const rOffset = { x: fmt(-data.offset.x), y: data.offset.y, z: data.offset.z };
            const rPivot  = { x: fmt(-data.pivot.x),  y: data.pivot.y,  z: data.pivot.z  };
            lines.push(`${I2}  - type: RIGHT`);
            lines.push(`${I2}    front: ${front}`);
            lines.push(`${I2}    material: PAPER`);
            lines.push(`${I2}    custom-model-data: ${wheelCmd}`);
            lines.push(`${I2}    offset:`);
            lines.push(vec3yaml(rOffset, `${I2}      `));
            lines.push(`${I2}    pivot:`);
            lines.push(vec3yaml(rPivot, `${I2}      `));
        }

        // seat
        const seat = partData.seat;
        if (seat) {
            lines.push(`${I2}seat:`);
            lines.push(`${I2}  offset:`);
            lines.push(vec3yaml(seat.offset, `${I2}    `));
        }

        // physics defaults
        lines.push(`${I2}physics:`);
        lines.push(`${I2}  max-speed: 1.2`);
        lines.push(`${I2}  acceleration: 0.05`);
        lines.push(`${I2}  friction: 0.02`);
        lines.push(`${I2}  turn-rate: 3.5`);
        lines.push(`${I2}  brake-force: 0.08`);
        lines.push(`${I2}  reverse-max-speed: 0.4`);
        lines.push(`${I2}  reverse-acceleration: 0.03`);
        lines.push(`${I2}  drift-brake-force: 0.04`);
        lines.push(`${I2}  drift-turn-multiplier: 1.8`);
        lines.push(`${I2}  vehicle-width: 0.8`);
        lines.push(`${I2}  vehicle-length: 1.2`);

        return lines.join("\n");
    }

    // ── Dialog ─────────────────────────────────────────────────────────────

    function openDialog() {
        const required = ["frame","steering_wheel","door_0","door_1","wheel_0","seat"];
        const missing  = required.filter(k => !findGroup(k));
        const hasWheel1 = !!findGroup("wheel_1");

        let notice = missing.length > 0
            ? `Missing groups: ${missing.join(", ")}`
            : "All required groups found.";
        if (!hasWheel1) notice += "\n(wheel_1 not found — wheel_0 used for rear too)";

        new Dialog({
            id: "uvp_input",
            title: "Export Vehicle Config",
            form: {
                notice:    { label: "Status",             type: "info", text: notice },
                vehicleId: { label: "Vehicle ID",         type: "text",   value: "my_car" },
                frameCmd:  { label: "Frame CMD",          type: "number", value: 1 },
                swCmd:     { label: "Steering Wheel CMD", type: "number", value: 2 },
                d0Cmd:     { label: "Door Left CMD",      type: "number", value: 3 },
                d1Cmd:     { label: "Door Right CMD",     type: "number", value: 3 },
                wCmd:      { label: "Wheel CMD (all 4)",  type: "number", value: 4 },
            },
            onConfirm(data) {
                const vehicleId = data.vehicleId || "my_car";
                const frameCmd  = data.frameCmd  || 1;
                const swCmd     = data.swCmd     || 2;
                const d0Cmd     = data.d0Cmd     || 3;
                const d1Cmd     = data.d1Cmd     || 3;
                const wCmd      = data.wCmd      || 4;

                const partData = {};
                for (const key of ["frame","steering_wheel","door_0","door_1","wheel_0","wheel_1","seat"]) {
                    partData[key] = extractPart(key);
                }

                const yaml = generateYaml(vehicleId, partData, wCmd, [d0Cmd, d1Cmd], frameCmd, swCmd);
                showResult(yaml);
            }
        }).show();
    }

    function showResult(yaml) {
        new Dialog({
            id: "uvp_result",
            title: "Exported Vehicle Config — copy into config.yml",
            form: {
                yaml: { label: "YAML", type: "textarea", value: yaml },
            },
            buttons: ["Close"],
        }).show();
    }

    // ── Register ───────────────────────────────────────────────────────────

    Plugin.register("unique_vehicle_config_exporter", {
        title:       "UniqueVehicle Config Exporter",
        author:      "UniqueVehiclePlugin",
        description: "Exports group origins as config.yml offsets/pivots for UniqueVehiclePlugin.",
        version:     "1.1.0",
        min_version: "4.8.0",
        variant:     "both",

        onload() {
            this.action = new Action("export_vehicle_config", {
                name:        "Export Vehicle Config",
                description: "Generate config.yml snippet for UniqueVehiclePlugin",
                icon:        "directions_car",
                click:       () => openDialog(),
            });
            MenuBar.addAction(this.action, "file.export");
        },

        onunload() {
            this.action.delete();
        },
    });
})();
