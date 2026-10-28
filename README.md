# GraphEditor

Графический редактор на Java (Swing). Реализован инкремент 1 — растровый редактор.

## Возможности

- Рабочая область для создания изображения (FR-1.1).
- Инструменты: точка, круг, квадрат; выбранный инструмент подсвечивается (FR-1.2, FR-1.9).
- Рисование мышью: при удержании левой кнопки элементы повторяют путь движения
  курсора выбранным цветом (FR-1.3, FR-1.4).
- Одновременное отображение множества элементов разных типов (FR-1.5–FR-1.7).
- Кнопка «Очистить» — удаление всех элементов (FR-1.8).
- Модель: абстрактный класс `Pixel` и наследники `Point`, `Circle`, `Square`;
  создание элементов вынесено в фабрику `PixelFactory`.

## Запуск

Требуется JDK 17+. Двойной клик по `run.bat` (запуск) или `test.bat` (тесты),
либо вручную:

```bash
javac -encoding UTF-8 -d out $(find src test -name "*.java")
java -cp out ru.grapheditor.TestRunner
java -cp out ru.grapheditor.ui.GraphEditor
```

## Структура

```text
src/ru/grapheditor/
├── pixel/   # Pixel (abstract), Point, Circle, Square, PixelType, PixelFactory
├── shape/   # Shape, Drawing
└── ui/      # GraphEditor, Canvas, Tool, ToolSelector
test/ru/grapheditor/TestRunner   # тесты T1–T15
docs/uml-class.puml              # UML-диаграмма классов
```
