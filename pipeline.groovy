pipelineJob('pipeline') {
  definition {
    cps {
      script(
'''pipeline{
    agent{
            docker { image 'pipeline/jenkins'
                        args '-u 0:0 -v /var/run/docker.sock:/var/run/docker.sock'}}
    stages{
        stage('git'){
            steps{
                echo 'cloning the repository'
                git branch: 'main', credentialsId: 'github-credentials', url: 'https://github.com/SamarMatoussi/JCasC-master.git\' 
                sh 'docker run hello-world'
            }
        }
        stage('unit_test'){
            steps{
                sh 'echo "start the unit test"'
            }
        }
        stage('build'){
            steps{
                sh 'echo "login to the github container registry"'
                withCredentials([usernamePassword(credentialsId: 'github-credentials', passwordVariable: 'PASSWORD', usernameVariable: 'USER')]) {
                    sh 'whoami'
                    sh 'echo "${USER}"'
                    sh 'echo "${PASSWORD}"'
                    sh 'docker login --username "${USER}" --password "${PASSWORD}" registry.github.com'
            }
                    sh 'echo "building and pushing images to the container registry"'
            }
        }
    }
}''')
    }
  }
}
