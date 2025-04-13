# Конвертер нотаций BPMN (.bpmn) в изображение PNG

## Сборка и запуск

- Установить docker
- Перейти в терминале в папку проекта
- Выполнить:

``` bash
docker-compose up --build -d
```

Если недостаточно прав

``` bash
sudo docker-compose up --build -d
```

## Пример запроса

curl -X POST http://localhost:3000/render \
-H "Content-Type: multipart/form-data" \
-F "file=@~/Downloads/source.bpmn" \
--output ~/Downloads/result.png

параметры:

- localhost:3000 - адрес и порт, на которых запущено приложение (по умолчанию порт 3000, можно изменить в
  docker-compose)
- ~/Downloads/source.bpmn - файл .bpmn
- ~/Downloads/result.png - имя изображения в формате .png

## Тесты

1. Проверка конвертации через curl

- Запустить контейнер, как описано в разделе *Сборка и запуск*
- В браузере перейти по адресу http://localhost:3000/api-docs/
- развернуть метод '/render', нажать 'Try it out', указать файл и формат, нажать 'Execute'
- результат можно скачать

2. Проверка конвертации через curl

Для запуска теста

- Запустить контейнер, как описано в разделе *Сборка и запуск*
- Перейти в терминале в папку проекта
- Выполнить команду:

``` bash
curl -X POST http://localhost:3000/render \
  -H "Content-Type: multipart/form-data" \
  -F "file=@./test/source.bpmn" \
  --output ./test/result.png
```

В результате в папке проекта, в подпапке 'test' появится файл 'result.png'

3. Запуск нескольких параллельных запросов

Для запуска теста

- Установить k6. Например, в Ubuntu командой:

``` bash 
sudo snap install k6
```

- Запустить контейнер, как описано в разделе *Сборка и запуск*
- Перейти в терминале в папку проекта
- Выполнить команду:

```bash
k6 run ./test/k6-render-test.js
```
