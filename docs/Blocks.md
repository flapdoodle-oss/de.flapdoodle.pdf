# Blocks

## Text

```java
Text text = new Text(someText);
```

![text.png](block-text.png)

[text.pdf](block-text.pdf)

## Title

```java
Section title = Section.builder()
  .title("Title")
  .font(new Font(Font.HELVETICA, 24, Font.BOLD))
  .build();
```

![title.png](block-title.png)

[title.pdf](block-title.pdf)

### Title (on new page)

```java
Text text = new Text(someText);
Section title = Section.builder()
  .title("Title")
  .font(new Font(Font.HELVETICA, 24, Font.BOLD))
  .build();
Section withSpace = Section.builder()
  .title("... need more NewLine:o")
  .font(new Font(Font.HELVETICA, 24, Font.BOLD))
  .minPageHeightLeft(PageSize.A4.getHeight()*0.66f)
  .build();
```

![title-on-new-page.png](block-title-newpage-0.png)
![title-on-new-page.png](block-title-newpage-1.png)

[title-on-new-page.pdf](block-title-newpage.pdf)