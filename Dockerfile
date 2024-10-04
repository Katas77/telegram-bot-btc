# Базовый образ, содержащий Јата 17
FROM openjdk:17-oracle

#Директория приложения внутри контейнера
WORKDIR /app

# Копирование JAR-файла приложения в контейнер
COPY build/libs/telegram-bot-gradle-0.0.1-SNAPSHOT.jar app.jar

#Команда docker build создает образы Docker из файла Dockerfile: docker build -t telegram_bot .
# Команда docker run Создает и запускает контейнер на основе заданного образа.: docker run telegram_bot
# docker run -it student-registration
CMD ["java", "-jar", "app.jar"]


#( в терминалеPS C:\Users\krp77\IdeaProjects\socketserver>  docker build -t telegram_bot .)
#(PS C:\Users\krp77\IdeaProjects\socketserver>  docker run telegram_bot  )

# docker container ls
# docker container ls -a
# docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' fa0ad4b31d4d
# docker image ls
#  docker image rm student-registration -f                             (dell image)
# docker start [container]	Запускает данный контейнер.

# docker network create spring-socket-network
# docker ps -a	Перечисляет все контейнеры. Пометка -a показывает как работающие, так и неработающие контейнеры. Чтобы отображать только запущенные контейнеры, эту пометку можно опустить.
# cd  (англ. change directory — изменить каталог) PS C:\Users\krp77> cd "C:\Users\krp77\Music"
#Переход на уровень выше в иерархии директорий:Windows: cd ..
 # Переход в корневую директорию:Windows: cd \