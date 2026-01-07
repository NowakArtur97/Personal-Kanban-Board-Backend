# Personal-Kanban-Board-Backend

## Table of Contents

- [General info](#general-info)
- [Features](#features)
- [Built With](#built-with)
- [Status](#status)

## General info

A backend application built using Spring and GraphQL to manage user tasks and subtasks.

Repository with frontend application: https://github.com/NowakArtur97/Personal-Kanban-Board-Frontend

## Features

Kanban Board:

- User login
- User registration
- Authentication and authorization using JWT
- GraphQL queries to retrieve:
    - users
    - data of the logged in user
    - tasks with subtasks
    - tasks with subtasks assigned to a specific user
- GraphQL mutations to:
    - register a user
    - create, update and delete tasks and subtasks
    - update the user assigned to tasks and subtasks
    - delete all subtasks from a task
    - delete all tasks and subtasks
- GraphQL subscriptions for frontend event handling in case of:
    - creating, updating and deleting tasks and subtasks
    - deleting all subtasks from a task
    - deleting all tasks and subtasks
- Possibility to use GraphQL Playground for application testing
- Data migration using Flyway scripts
- Possibility to view and edit data in the database using the H2 console
- Global error handling
- Defining error messages in the file for validation
- Testing with testcontainers

CloudFormation:

- Creating the resources needed to run a backend application on an EC2 instance with access to the database
- Exporting the backend urls to the Parameter Store so they can be used by the frontend application
- Automatic detection of changes in the GitHub repository, building and deploying application using CodeBuild,
  CodeDeploy and CodePipeline
- Automatic cleaning of the S3 bucket with application artifacts after deleting a CloudFormation template

## Built With

Backend build with:

- Java 21
- Spring (Boot, Webflux, GraphQL, Security, Data R2DBC, JPA) - 3.5.7
- Flyway - 11.7.2
- H2 database - 2.3.232
- Lombok - 1.18.42
- Testcontainers - 1.21.3
- GraphQL Java Extended Scalars (24.0)
- GraphQL Java Extended Validation (24.0)
- JSON Web Token Support For The JVM (jjwt) - 0.12.5
- Maven
- Docker Compose
- PostgreSQL
- pgAdmin 4

CloudFormation resources:

- VPC
- Internet Gateway
- VPC Gateway Attachment
- Route Table
- Route
- Subnet Route Table Association
- Subnet
- DB Subnet Group
- Security Group
- Security Group Ingress
- DB Instance
- Instance Profile
- IAM Roles
- EC2 Instance
- CodeBuild (Project)
- CodeDeploy (Application, Deployment Group)
- CodePipeline (Pipeline, Webhook)
- S3 Bucket
- Lambda Function
- CloudFormation Custom Resource
- Parameter Store

## Status

Project is: finished
