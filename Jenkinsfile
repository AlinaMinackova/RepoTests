pipeline {
    agent any

    environment {
        APP_IMAGE = "my-app:latest"
        BACKEND_REPO = "https://github.com/AlinaMinackova/RepoBackend.git"
    }

    triggers {
        // Автозапуск по пушам (можно заменить на webhook)
        pollSCM('* * * * *')
    }

    options {
        timestamps()  // чтобы в логе были таймстемпы
    }

    stages {
        stage('Checkout Backend') {
            steps {
                dir('backend') {
                    git branch: 'main', url: "${BACKEND_REPO}"
                }
            }
        }

        stage('Build Backend Jar & Docker Image') {
            steps {
                dir('backend') {
                    sh './mvnw clean package -DskipTests'
                    sh 'docker build -t my-app:latest .'
                }
            }
        }

        stage('Run Tests with Allure') {
            steps {
                echo "Запуск интеграционных тестов через Gradle..."
                sh './gradlew clean test'
            }

            post {
                always {
                    echo "Сохраняем результаты Allure..."
                    // Архивируем allure-results для отчёта
                    allure([
                        includeProperties: false,
                        jdk: '',
                        results: [[path: 'build/allure-results']]
                    ])
                }
            }
        }
    }

    post {
        always {
            echo "🧹 Очистка Docker окружения..."
            sh 'docker system prune -f'
        }

        success {
            echo '✅ Все тесты прошли успешно!'
        }

        failure {
            echo '❌ Ошибка при выполнении тестов!'
        }
    }
}