# Page Decorations
                  

## Background Image

```java
DocumentFactory factory = DocumentFactory.builder()
  .pageSize(PageSize.A4)
  .addBlocks(new Text("Background Image"))
  .addOnPageEvents(PageDecorator.builder()
    .elementFactory(pageNumber -> Optional.of(image))
    .boxFactory(doc -> PageBox.fullPageBox(doc)
      .boxAt(new Dimension(50.0f, 50.0f), HorizontalAlignment.LEFT, VerticalAlignment.TOP))
    .build())
  .build();
```

![backgroundImage.png](page-backgroundImage.png)

[backgroundImage.pdf](page-backgroundImage.pdf)

## Header and Footer

```java
PageDecorator header = PageDecorator.builder()
  .elementFactory(page -> Optional.of(TableElement.builder()
    .columns(TableElement.Columns.relativeWeights(1.0f))
    .addCells(PdfPCellFactory.builder()
      .phrase(PhraseElement.of("Page "+page))
      .cellStyle(CellStyle.empty()
        .withHorizontalAlignment(HorizontalAlignment.CENTER)
        .withBorder(BorderProperty.<BorderStyle>of(BorderStyle.noBorder())
          .withBottom(BorderStyle.of(Color.BLACK, 0.5f))))
      .build())
    .build().create()))
  .boxFactory(document -> PageBox.fullPageBox(document)
    .rowAt(20.0f, VerticalAlignment.TOP))
  .build();

PageDecorator footer = PageDecorator.builder()
  .elementFactory(page -> Optional.of(TableElement.builder()
    .columns(TableElement.Columns.relativeWeights(1.0f))
    .addCells(PdfPCellFactory.builder()
      .phrase(PhraseElement.of("-- Page "+page+" --"))
      .cellStyle(CellStyle.empty()
        .withHorizontalAlignment(HorizontalAlignment.CENTER)
        .withBorder(BorderProperty.<BorderStyle>of(BorderStyle.noBorder())
          .withTop(BorderStyle.of(Color.BLACK, 0.5f))))
      .build())
    .build().create()))
  .boxFactory(document -> PageBox.fullPageBox(document)
    .rowAt(20.0f, VerticalAlignment.BOTTOM))
  .build();

DocumentFactory factory = DocumentFactory.builder()
  .pageSize(PageSize.A4)
  .addBlocks(new Text("Header and Footer"))
  .addOnPageEvents(header, footer)
  .build();
```

![pageHeaderAndFooter.png](page-header.png)

[pageHeaderAndFooter.pdf](page-header.pdf)
