#!/usr/bin/env node
/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

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
