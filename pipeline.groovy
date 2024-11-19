pipelineJob('pipeline') {
  definition {
    cps {
      script(
'''pipeline {
    agent any
    environment {
        registry = "samarmatoussi/atlaslabs"
        registryCredential = 'dockerhub-credentials'-
        backendDockerImage = "samarmatoussi/atlaslabs-backend"
        frontendDockerImage = "samarmatoussi/atlaslabs-frontend"
    }

    stages {
        stage('CHECKOUT GIT') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
                    git branch: 'main', url: "https://${GIT_USER}:${GIT_PASS}@github.com/SamarMatoussi/AtlasLabs.git"
                }
            }
        }

        stage('MAVEN CLEAN') {
            steps {
                dir('backend') {
                    sh 'mvn clean'
                }
            }
        }

        stage('ARTIFACT CONSTRUCTION') {
            steps {
                dir('backend') {
                    sh 'mvn package'
                }
            }
        }

        stage('SONARQUBE ANALYSIS') {
            steps {
                dir('backend') {
                    withCredentials([usernamePassword(credentialsId: 'sonarqube-credentials', usernameVariable: 'SONAR_USER', passwordVariable: 'SONAR_PASS')]) {
                        sh 'mvn sonar:sonar -Dsonar.login=$SONAR_USER -Dsonar.password=$SONAR_PASS'
                    }
                }
            }
        }

        stage('RUN TESTS') {
            steps {
                dir('backend') {
                    echo 'Launching Unit Tests...'
                    sh 'mvn test'
                }
            }
        }

        stage('DOCKER BUILD BACKEND') {
            steps {
                script {
                    docker.build("${backendDockerImage}:${BUILD_NUMBER}", "backend/").push()
                }
            }
        }

        stage('DOCKER BUILD FRONTEND') {
            steps {
                script {
                    docker.build("${frontendDockerImage}:${BUILD_NUMBER}", "frontend/").push()
                }
            }
        }
    }

    post {
        success {
            emailext(
                subject: "Pipeline terminé avec succès !",
                body: "Le pipeline a été exécuté avec succès.",
                to: "matoussi.samar20@gmail.com"
            )
        }
        failure {
            emailext(
                subject: "Échec du pipeline",
                body: "Le pipeline a échoué. Veuillez vérifier les logs Jenkins.",
                to: "matoussi.samar20@gmail.com"
            )
        }
    }
}
''')
    }
  }
}
