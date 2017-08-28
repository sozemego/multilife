pipeline {
    agent any

    tools {
        maven 'Maven 3.3.9'
    }

    environment {
        SERVER_JAR_PATH = "${env.MULTILIFE_ROOT}/server/target/multilife.jar"
        SERVER_RUNNER_JAR_PATH = "${env.MULTILIFE_ROOT}/serverrunner/target/ServerRunner.jar"
        CLIENT_DIRECTORY_PATH = "${env.MULTILIFE_ROOT}/client/build"
        PSCP_PATH = "${env.PSCP_PATH}"
        SERVER_PASS = "${env.SERVER_PASS}"
        SERVER_HOST = "${env.SERVER_HOST}"
    }

    stages {

        stage('Checkout') {
            steps {
                git 'https://www.github.com/sozemego/multilife'
            }
        }

        stage('Build client') {
            steps {
                dir('client') {
                    bat 'npm install'
                    bat 'npm run build'
                }
            }
        }

        stage('Copy client') {
            steps {
                bat '\"' + PSCP_PATH + '\" -r -pw ' + SERVER_PASS + ' \"' + CLIENT_DIRECTORY_PATH + '\" ' + SERVER_HOST
            }
        }

        stage('Build server') {
            steps {
                dir('server') {
                    bat 'mvn compile assembly:single'
                }
            }
        }

        stage('Copy to deployment machine') {
            steps {
                dir('server/target') {
                    bat 'del multilife.jar || true'
                    bat 'rename server-0.1-jar-with-dependencies.jar multilife.jar'
                }
                bat '\"' + PSCP_PATH + '\" -pw ' + SERVER_PASS + ' \"' + SERVER_JAR_PATH + '\" ' + SERVER_HOST
            }
        }

        stage('Server-runner') {
            steps {
                dir('serverrunner') {
                    bat 'mvn clean compile install'
                }
                dir('serverrunner/target') {
                    bat 'del ServerRunner.jar || true'
                    bat 'rename server-runner-1.0-SNAPSHOT.jar ServerRunner.jar'
                }
                bat '\"' + PSCP_PATH + '\" -pw ' + SERVER_PASS + ' \"' + SERVER_RUNNER_JAR_PATH + '\" ' + SERVER_HOST
            }
        }

    }
}