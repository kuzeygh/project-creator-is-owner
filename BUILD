load("//tools/bzl:plugin.bzl", "gerrit_plugin")

gerrit_plugin(
    name = "project-creator-is-owner",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: project-creator-is-owner",
        "Gerrit-Module: com.nabisoft.gerrit.plugins.projectcreatorisowner.ProjectCreatorIsOwner$Module",
        "Implementation-Title: Making the project creator the initial project owner",
        "Implementation-Version: 0.1",
        "Implementation-Vendor: nabisoft GmbH",
    ],
    resources = glob(["src/main/resources/**/*"]),
)
