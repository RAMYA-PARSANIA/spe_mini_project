// Declarative CI/CD pipeline for the Calculator REST API.
// Checkout -> Build -> Test -> Docker Build -> Push -> Deploy, with e-mail notification.
//
// Works on both Linux and Windows Jenkins agents: cmd() runs `sh` on Unix and `bat` on Windows.

def cmd(String command) {
    if (isUnix()) {
        sh command
    } else {
        bat command
    }
}

pipeline {
    agent any

    tools {
        // Name of the Maven installation in Manage Jenkins > Tools > Maven installations
        maven 'Maven-3.9'
    }

    environment {
        // Not secrets: the Docker Hub namespace is public. The password/token lives in
        // the Jenkins Credentials store under the ID 'dockerhub-credentials'.
        DOCKERHUB_USER = 'itsrp'
        DOCKER_IMAGE   = "${DOCKERHUB_USER}/calculator"
        NOTIFY_EMAIL   = 'ramyaparsania3@gmail.com'
        HTTP_PORT      = '8081'
    }

    triggers {
        // Build on every push: GitHub webhook (preferred) ...
        githubPush()
        // ... with Poll SCM as a fallback when GitHub cannot reach this Jenkins
        pollSCM('H/2 * * * *')
    }

    options {
        disableConcurrentBuilds()          // two deploys must never race each other
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '15'))
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                cmd 'git log -1 --oneline'
            }
        }

        stage('Build') {
            steps {
                cmd 'mvn -B clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/calculator.jar', fingerprint: true
                }
            }
        }

        stage('Test') {
            steps {
                // A failing test makes mvn exit non-zero, which fails this stage and the build
                cmd 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                cmd "docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} -t ${DOCKER_IMAGE}:latest ."
            }
        }

        stage('Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials',
                                                  usernameVariable: 'DOCKERHUB_USERNAME',
                                                  passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    script {
                        // Single quotes: the secret is expanded by the shell, never by Groovy,
                        // so it does not appear in the build log or the process list.
                        if (isUnix()) {
                            sh 'echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin'
                        } else {
                            bat '@echo %DOCKERHUB_TOKEN%| docker login -u %DOCKERHUB_USERNAME% --password-stdin'
                        }
                    }
                    cmd "docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}"
                    cmd "docker push ${DOCKER_IMAGE}:latest"
                }
            }
            post {
                always {
                    cmd 'docker logout'
                }
            }
        }

        stage('Deploy') {
            steps {
                withEnv(["IMAGE_TAG=${BUILD_NUMBER}"]) {
                    // Idempotent: Compose only recreates containers whose image/config changed.
                    // --wait blocks until every service reports healthy (or fails after the timeout).
                    cmd 'docker-compose up -d --remove-orphans --wait --wait-timeout 180'
                    cmd 'docker-compose ps'
                }
                // Verify the full path nginx -> calculator from inside the proxy container
                cmd 'docker-compose exec -T nginx wget -qO- http://127.0.0.1/actuator/health'
                cmd 'docker-compose exec -T nginx wget -qO- "http://127.0.0.1/api/v1/add?a=2&b=3"'
            }
            post {
                failure {
                    script {
                        // Roll back to the last image that deployed successfully, so a bad
                        // build never leaves the running deployment broken.
                        def previous = currentBuild.previousSuccessfulBuild?.number
                        if (previous) {
                            echo "Deploy failed - rolling back to build #${previous}"
                            withEnv(["IMAGE_TAG=${previous}"]) {
                                cmd 'docker-compose up -d --remove-orphans --wait --wait-timeout 180'
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            emailext(
                to: "${NOTIFY_EMAIL}",
                subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                mimeType: 'text/html',
                body: """<p>Build <b>#${env.BUILD_NUMBER}</b> of <b>${env.JOB_NAME}</b> succeeded.</p>
                         <p>Image pushed: <code>${DOCKER_IMAGE}:${env.BUILD_NUMBER}</code><br/>
                         Deployed with Docker Compose and health check passed.</p>
                         <p><a href="${env.BUILD_URL}">Open build in Jenkins</a></p>"""
            )
        }
        failure {
            emailext(
                to: "${NOTIFY_EMAIL}",
                subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                mimeType: 'text/html',
                attachLog: true,
                body: """<p>Build <b>#${env.BUILD_NUMBER}</b> of <b>${env.JOB_NAME}</b> failed.</p>
                         <p>The console log is attached.
                         <a href="${env.BUILD_URL}console">Open console output</a></p>"""
            )
        }
        always {
            // Remove dangling image layers left behind by rebuilds
            cmd 'docker image prune -f'
        }
    }
}
