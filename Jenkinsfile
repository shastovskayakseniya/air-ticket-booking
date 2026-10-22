pipeline {
    agent any

    stages {
        stage('Java Version') {
            steps {
                bat '"C:\\Program Files\\apache-maven-3.9.16\\bin\\mvn.cmd" -version'
            }
        }

        stage('Build') {
            steps {
                bat '"C:\\Program Files\\apache-maven-3.9.16\\bin\\mvn.cmd" compile'
            }
        }

        stage('Test') {
            steps {
                bat '"C:\\Program Files\\apache-maven-3.9.16\\bin\\mvn.cmd" test'
            }
        }

        stage('Package') {
            steps {
                bat '"C:\\Program Files\\apache-maven-3.9.16\\bin\\mvn.cmd" package -DskipTests'
            }
        }
    }
}