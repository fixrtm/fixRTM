#!/usr/bin/env node
const fs = require("fs");

const out_json = process.argv[2]
const release_note_path = process.argv[3]
const game_versions = process.argv[4]
const release_type = process.argv[5]

const changelog = {
    changelog: fs.readFileSync(release_note_path, "utf8"),
    changelogType: "markdown",
    // A list of supported game versions, see the Game Versions API for details.
    gameVersions: game_versions.split(",").map(x => Number(x)),
    releaseType: release_type,
    relations: {
        projects: [
            {
                slug: "realtrainmod",
                type: "requiredDependency",
            },
        ],
    },
}

fs.writeFileSync(out_json, JSON.stringify(changelog))
