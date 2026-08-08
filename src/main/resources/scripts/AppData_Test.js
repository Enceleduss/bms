// src/main/resources/scripts/AppData_Test.js

console.log("Starting AppData_Test.js script...");

// 1. Define a new node type "Project" inheriting from "GraphNode"
// Note: SchemaService.defineType(typeName, parentType)
schema.defineItem("Project", "GraphNode");

// 2. Declare attributes for the "Project" type
// Note: SchemaService.defineAttribute(typeName, attrName, dataType)
schema.defineAttribute("Project", "projectName", "string");
schema.defineAttribute("Project", "projectDescription", "text");
schema.defineAttribute("Project", "budget", "number");
schema.defineAttribute("Project", "isActive", "boolean");
schema.defineAttribute("Project", "startDate", "date");

console.log("Project node type and attributes defined successfully in AppData_Test.js.");
