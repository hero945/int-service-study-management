package com.huadong.pipeline.service.export;

import com.huadong.pipeline.manager.MonthlyExportManager.ExportProgress;
import com.huadong.pipeline.manager.MonthlyExportManager.ExportReport;
import com.huadong.pipeline.manager.MonthlyExportManager.ExportRisk;
import com.huadong.pipeline.manager.MonthlyExportManager.ExportSnapshotGroup;
import com.huadong.pipeline.manager.MonthlyExportManager.ExportSnapshotRow;
import com.huadong.pipeline.manager.MonthlyExportManager.ExportSummary;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Builds downloadable monthly-export payloads (HTML / CSV / XLSX). */
public final class MonthlyExportFileBuilder {

  private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
  private static final DateTimeFormatter DATE_TIME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

  private MonthlyExportFileBuilder() {}

  public static byte[] html(ExportReport report) {
    return renderHtmlDocument(report).getBytes(StandardCharsets.UTF_8);
  }

  public static String renderHtmlDocument(ExportReport report) {
    StringBuilder html = new StringBuilder();
    html.append("""
        <!DOCTYPE html>
        <html lang="zh-CN">
        <head>
        <meta charset="utf-8">
        <title>临床研发管线月度报告</title>
        <style>
        :root{--ink:#1a1d23;--muted:#5b6472;--line:#e7e9ed;--slate:#1a365d;--canvas:#f5f6f8;
          --teal:#0f766e;--coral:#c0362c;--amber:#a85e0c;--gray:#8a929e}
        *{box-sizing:border-box}body{margin:0;background:var(--canvas);color:var(--ink);
          font:13px/1.55 "IBM Plex Sans","PingFang SC","Microsoft YaHei",sans-serif}
        .sheet{max-width:920px;margin:24px auto;background:#fff;border:1px solid var(--line);
          box-shadow:0 8px 28px rgba(26,29,35,.08)}
        .masthead{background:var(--slate);color:#fff;padding:22px 28px 18px}
        .masthead h1{margin:0;font-size:22px;font-weight:650;letter-spacing:.2px}
        .meta{padding:14px 28px 10px;border-bottom:1px solid var(--line);color:var(--muted);font-size:12px}
        .meta strong{color:var(--ink);font-weight:600}
        .meta .right{float:right}
        .body{padding:8px 28px 28px}
        .sec{margin-top:22px}
        .sec-h{display:flex;align-items:baseline;gap:10px;border-bottom:1px solid var(--line);
          padding-bottom:8px;margin-bottom:14px}
        .sec-num{color:#7aa2d8;font:700 18px/1 "IBM Plex Mono",monospace}
        .sec-h h2{margin:0;font-size:15px}
        .metrics{display:grid;grid-template-columns:repeat(5,1fr);gap:0;border:1px solid var(--line)}
        .metrics-4{grid-template-columns:repeat(4,1fr)}
        .metric{padding:12px 10px;text-align:center;border-right:1px solid var(--line)}
        .metric:last-child{border-right:0}
        .metric .lbl{color:var(--muted);font-size:11px}
        .metric .val{font:700 22px/1.2 "IBM Plex Mono",monospace;margin-top:4px}
        .metric-note{margin-top:10px;color:var(--muted);font-size:12px}
        .ta-bar{margin:14px 0 0;padding:7px 10px;background:#eef2f7;color:#2a3650;
          font-weight:600;font-size:12px}
        table{width:100%;border-collapse:collapse;font-size:12px}
        th,td{padding:8px 10px;border-bottom:1px solid var(--line);text-align:left;vertical-align:top}
        th{color:var(--muted);font-weight:600;background:#fbfcfd}
        .mono{font-family:"IBM Plex Mono",monospace}
        .st-active{color:var(--teal);text-decoration:underline;text-underline-offset:2px}
        .st-done{color:#177245;text-decoration:underline;text-underline-offset:2px}
        .st-prep{color:var(--gray);text-decoration:underline;text-underline-offset:2px}
        .st-delay{color:var(--coral);text-decoration:underline;text-underline-offset:2px}
        .prog{display:grid;grid-template-columns:64px 1fr;gap:0 14px;margin:0 0 16px}
        .prog-date{font:600 12px/1.4 "IBM Plex Mono",monospace;color:var(--muted);padding-top:2px}
        .prog-study{font:650 13px/1.4 "IBM Plex Mono",monospace}
        .prog-meta{color:var(--muted);font-size:11.5px;margin:2px 0 6px}
        .prog-line{margin:6px 0 0}
        .pill{display:inline-block;font-size:10.5px;font-weight:700;letter-spacing:.3px;
          text-transform:uppercase;background:#f1f3f6;color:#3b424e;border-radius:4px;
          padding:2px 6px;margin-right:8px}
        .risk{display:grid;grid-template-columns:36px 1fr auto;gap:10px;align-items:start;
          padding:10px 0;border-bottom:1px solid var(--line)}
        .risk-idx{font:700 12px "IBM Plex Mono",monospace;color:var(--muted)}
        .risk-score{font:700 20px/1 "IBM Plex Mono",monospace}
        .score-high{color:var(--coral)}.score-med{color:var(--amber)}.score-low{color:#596273}
        .foot{padding:16px 28px 22px;text-align:center;color:#9aa2ad;font-size:11px;
          border-top:1px solid var(--line)}
        @media print{body{background:#fff}.sheet{margin:0;border:0;box-shadow:none}}
        </style>
        </head>
        <body><div class="sheet">
        """);
    html.append("<div class=\"masthead\"><h1>临床研发管线月度报告</h1></div>");
    html.append("<div class=\"meta\"><div class=\"right\">生成于 ")
        .append(esc(DATE_TIME.format(report.meta().generatedAt())))
        .append("</div><div>汇报时间段 <strong>")
        .append(esc(DATE.format(report.meta().startDate())))
        .append(" 至 ")
        .append(esc(DATE.format(report.meta().endDate())))
        .append("</strong></div><div>导出范围 · ")
        .append(esc(String.join("、", report.meta().scopeLabels())))
        .append("</div></div><div class=\"body\">");

    ExportSummary s = report.summary();
    html.append("""
        <section class="sec"><div class="sec-h"><span class="sec-num">01</span><h2>Study 汇总</h2></div>
        <div class="metrics metrics-4">
        """);
    appendMetric(html, "总数", s.total());
    appendMetric(html, "未开始", s.notStarted());
    appendMetric(html, "进行中", s.inProgress());
    appendMetric(html, "已完成", s.completed());
    html.append("</div><div class=\"metric-note\">有填报 Study ")
        .append(s.reportedStudyCount())
        .append(" · Open 风险 ")
        .append(s.openRiskCount())
        .append("</div></section>");

    html.append("""
        <section class="sec"><div class="sec-h"><span class="sec-num">02</span><h2>管线快照</h2></div>
        """);
    if (report.snapshotGroups().isEmpty()) {
      html.append("<p style=\"color:#9aa2ad\">当前范围内暂无 Study。</p>");
    } else {
      for (ExportSnapshotGroup group : report.snapshotGroups()) {
        String title = group.taName().isBlank() ? group.taCode() : group.taName();
        if (!group.taCode().isBlank() && !group.taName().isBlank()
            && !group.taCode().equals(group.taName())) {
          title = group.taName() + " · " + group.taCode();
        }
        html.append("<div class=\"ta-bar\">").append(esc(title)).append("</div>");
        html.append("""
            <table><thead><tr>
            <th>Program</th><th>Study</th><th>适应症</th><th>阶段</th><th>状态</th>
            </tr></thead><tbody>
            """);
        for (ExportSnapshotRow row : group.rows()) {
          html.append("<tr><td class=\"mono\">").append(esc(row.programCode()))
              .append("</td><td class=\"mono\">").append(esc(row.studyCode()))
              .append("</td><td>").append(esc(row.indication()))
              .append("</td><td>").append(esc(row.phase()))
              .append("</td><td class=\"").append(statusClass(row.projectStatus())).append("\">")
              .append(esc(row.projectStatus())).append("</td></tr>");
        }
        html.append("</tbody></table>");
      }
    }
    html.append("</section>");

    html.append("""
        <section class="sec"><div class="sec-h"><span class="sec-num">03</span><h2>月报进展</h2></div>
        """);
    if (report.progress().isEmpty()) {
      html.append("<p style=\"color:#9aa2ad\">所选时间段内暂无月报进展。</p>");
    } else {
      for (ExportProgress item : report.progress()) {
        html.append("<div class=\"prog\"><div class=\"prog-date\">")
            .append(esc(formatMd(item.entryDate())))
            .append("</div><div><div class=\"prog-study\">")
            .append(esc(item.studyCode()))
            .append("</div><div class=\"prog-meta\">")
            .append(esc(item.programCode())).append(" · ").append(esc(item.taName()))
            .append("</div><div class=\"prog-line\"><span class=\"pill\">")
            .append(esc(item.functionCode())).append("</span>")
            .append(esc(item.content())).append("</div></div></div>");
      }
    }
    html.append("</section>");

    html.append("""
        <section class="sec"><div class="sec-h"><span class="sec-num">04</span><h2>未关闭风险</h2></div>
        """);
    if (report.openRisks().isEmpty()) {
      html.append("<p style=\"color:#9aa2ad\">当前范围内无 Open 风险。</p>");
    } else {
      int i = 1;
      for (ExportRisk risk : report.openRisks()) {
        html.append("<div class=\"risk\"><div class=\"risk-idx\">")
            .append(String.format("%02d", i++))
            .append("</div><div><div class=\"mono\" style=\"font-weight:650\">")
            .append(esc(risk.riskCode()))
            .append("</div><div style=\"margin-top:3px\">")
            .append(esc(risk.description()))
            .append("</div><div style=\"margin-top:4px;color:#6e7681;font-size:11.5px\">")
            .append(esc(risk.programCode())).append(" · ").append(esc(risk.ownerName()))
            .append("</div></div><div class=\"risk-score ")
            .append(scoreClass(risk.level()))
            .append("\">").append(risk.score()).append("</div></div>");
      }
    }
    html.append("</section></div>");
    html.append("<div class=\"foot\">临床研发平台 · 月报导出 · 机密</div></div></body></html>");
    return html.toString();
  }

  public static byte[] csv(ExportReport report) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(0xEF);
    out.write(0xBB);
    out.write(0xBF);
    try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
      writer.write("汇报开始,汇报结束,TA,Program,Study,功能线代码,功能线名称,进展日期,月报进展\n");
      String start = DATE.format(report.meta().startDate());
      String end = DATE.format(report.meta().endDate());
      for (ExportProgress item : report.progress()) {
        writer.write(csv(start));
        writer.write(',');
        writer.write(csv(end));
        writer.write(',');
        writer.write(csv(item.taName()));
        writer.write(',');
        writer.write(csv(item.programCode()));
        writer.write(',');
        writer.write(csv(item.studyCode()));
        writer.write(',');
        writer.write(csv(item.functionCode()));
        writer.write(',');
        writer.write(csv(item.functionName()));
        writer.write(',');
        writer.write(csv(DATE.format(item.entryDate())));
        writer.write(',');
        writer.write(csv(item.content()));
        writer.write('\n');
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return out.toByteArray();
  }

  public static byte[] xlsx(ExportReport report) {
    SharedStrings sst = new SharedStrings();
    String sheet1 = buildSummarySheet(report, sst);
    String sheet2 = buildSnapshotSheet(report, sst);
    String sheet3 = buildProgressSheet(report, sst);
    String sheet4 = buildRiskSheet(report, sst);
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try (ZipOutputStream zip = new ZipOutputStream(out)) {
        write(zip, "[Content_Types].xml", CONTENT_TYPES);
        write(zip, "_rels/.rels", ROOT_RELS);
        write(zip, "xl/workbook.xml", WORKBOOK);
        write(zip, "xl/_rels/workbook.xml.rels", WORKBOOK_RELS);
        write(zip, "xl/styles.xml", STYLES);
        write(zip, "xl/sharedStrings.xml", sst.xml());
        write(zip, "xl/worksheets/sheet1.xml", sheet1);
        write(zip, "xl/worksheets/sheet2.xml", sheet2);
        write(zip, "xl/worksheets/sheet3.xml", sheet3);
        write(zip, "xl/worksheets/sheet4.xml", sheet4);
      }
      return out.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void appendMetric(StringBuilder html, String label, long value) {
    html.append("<div class=\"metric\"><div class=\"lbl\">").append(esc(label))
        .append("</div><div class=\"val\">").append(value).append("</div></div>");
  }

  private static String statusClass(String status) {
    return switch (status) {
      case "进行中" -> "st-active";
      case "已完成" -> "st-done";
      default -> "st-prep";
    };
  }

  private static String scoreClass(String level) {
    return switch (level) {
      case "HIGH" -> "score-high";
      case "MEDIUM" -> "score-med";
      default -> "score-low";
    };
  }

  private static String formatMd(java.time.LocalDate date) {
    return String.format("%02d-%02d", date.getMonthValue(), date.getDayOfMonth());
  }

  private static String esc(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  private static String csv(String value) {
    if (value == null) {
      return "\"\"";
    }
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }

  private static void write(ZipOutputStream zip, String name, String content) throws IOException {
    zip.putNextEntry(new ZipEntry(name));
    zip.write(content.getBytes(StandardCharsets.UTF_8));
    zip.closeEntry();
  }

  private static final String CONTENT_TYPES = """
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
        <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
        <Default Extension="xml" ContentType="application/xml"/>
        <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
        <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
        <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
        <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
        <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
        <Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
        <Override PartName="/xl/worksheets/sheet4.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
      </Types>
      """;

  private static final String ROOT_RELS = """
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
        <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
      </Relationships>
      """;

  private static final String WORKBOOK = """
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
        xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
        <sheets>
          <sheet name="汇总" sheetId="1" r:id="rId1"/>
          <sheet name="管线快照" sheetId="2" r:id="rId2"/>
          <sheet name="月报进展" sheetId="3" r:id="rId3"/>
          <sheet name="Open风险" sheetId="4" r:id="rId4"/>
        </sheets>
      </workbook>
      """;

  private static final String WORKBOOK_RELS = """
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
        <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
        <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
        <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet3.xml"/>
        <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet4.xml"/>
        <Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
        <Relationship Id="rId6" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
      </Relationships>
      """;

  private static final String STYLES = """
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
        <fonts count="1"><font><sz val="11"/><name val="Calibri"/></font></fonts>
        <fills count="1"><fill><patternFill patternType="none"/></fill></fills>
        <borders count="1"><border/></borders>
        <cellStyleXfs count="1"><xf/></cellStyleXfs>
        <cellXfs count="1"><xf xfId="0"/></cellXfs>
      </styleSheet>
      """;

  private static final class SharedStrings {
    private final List<String> values = new ArrayList<>();
    private final Map<String, Integer> index = new HashMap<>();

    int add(String value) {
      String key = value == null ? "" : value;
      Integer existing = index.get(key);
      if (existing != null) {
        return existing;
      }
      int next = values.size();
      values.add(key);
      index.put(key, next);
      return next;
    }

    String xml() {
      StringBuilder sb = new StringBuilder();
      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
      sb.append("<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"")
          .append(values.size()).append("\" uniqueCount=\"").append(values.size()).append("\">");
      for (String value : values) {
        sb.append("<si><t xml:space=\"preserve\">").append(xmlEsc(value)).append("</t></si>");
      }
      sb.append("</sst>");
      return sb.toString();
    }
  }

  private static String buildSummarySheet(ExportReport report, SharedStrings sst) {
    ExportSummary s = report.summary();
    StringBuilder rows = new StringBuilder();
    int r = 1;
    rows.append(row(r++, List.of(cellS(sst.add("指标")), cellS(sst.add("数值")))));
    rows.append(row(r++, List.of(cellS(sst.add("Study总数")), cellN(s.total()))));
    rows.append(row(r++, List.of(cellS(sst.add("未开始")), cellN(s.notStarted()))));
    rows.append(row(r++, List.of(cellS(sst.add("进行中")), cellN(s.inProgress()))));
    rows.append(row(r++, List.of(cellS(sst.add("已完成")), cellN(s.completed()))));
    rows.append(row(r++, List.of(cellS(sst.add("有填报Study数")), cellN(s.reportedStudyCount()))));
    rows.append(row(r++, List.of(cellS(sst.add("Open风险数")), cellN(s.openRiskCount()))));
    rows.append(row(r++, List.of(cellS(sst.add("汇报开始")),
        cellS(sst.add(DATE.format(report.meta().startDate()))))));
    rows.append(row(r++, List.of(cellS(sst.add("汇报结束")),
        cellS(sst.add(DATE.format(report.meta().endDate()))))));
    rows.append(row(r, List.of(cellS(sst.add("导出范围")),
        cellS(sst.add(String.join("、", report.meta().scopeLabels()))))));
    return sheetXml(rows.toString());
  }

  private static String buildSnapshotSheet(ExportReport report, SharedStrings sst) {
    StringBuilder rows = new StringBuilder();
    int r = 1;
    rows.append(row(r++, List.of(
        cellS(sst.add("TA")),
        cellS(sst.add("Program")),
        cellS(sst.add("Study")),
        cellS(sst.add("适应症")),
        cellS(sst.add("阶段")),
        cellS(sst.add("状态")))));
    for (ExportSnapshotGroup group : report.snapshotGroups()) {
      for (ExportSnapshotRow item : group.rows()) {
        rows.append(row(r++, List.of(
            cellS(sst.add(group.taName())),
            cellS(sst.add(item.programCode())),
            cellS(sst.add(item.studyCode())),
            cellS(sst.add(item.indication())),
            cellS(sst.add(item.phase())),
            cellS(sst.add(item.projectStatus())))));
      }
    }
    return sheetXml(rows.toString());
  }

  private static String buildProgressSheet(ExportReport report, SharedStrings sst) {
    StringBuilder rows = new StringBuilder();
    int r = 1;
    rows.append(row(r++, List.of(
        cellS(sst.add("进展日期")),
        cellS(sst.add("Study")),
        cellS(sst.add("Program")),
        cellS(sst.add("TA")),
        cellS(sst.add("功能线代码")),
        cellS(sst.add("功能线名称")),
        cellS(sst.add("进展内容")))));
    for (ExportProgress item : report.progress()) {
      rows.append(row(r++, List.of(
          cellS(sst.add(DATE.format(item.entryDate()))),
          cellS(sst.add(item.studyCode())),
          cellS(sst.add(item.programCode())),
          cellS(sst.add(item.taName())),
          cellS(sst.add(item.functionCode())),
          cellS(sst.add(item.functionName())),
          cellS(sst.add(item.content())))));
    }
    return sheetXml(rows.toString());
  }

  private static String buildRiskSheet(ExportReport report, SharedStrings sst) {
    StringBuilder rows = new StringBuilder();
    int r = 1;
    rows.append(row(r++, List.of(
        cellS(sst.add("Risk ID")),
        cellS(sst.add("Program")),
        cellS(sst.add("风险描述")),
        cellS(sst.add("总分")),
        cellS(sst.add("等级")),
        cellS(sst.add("Owner")))));
    for (ExportRisk risk : report.openRisks()) {
      rows.append(row(r++, List.of(
          cellS(sst.add(risk.riskCode())),
          cellS(sst.add(risk.programCode())),
          cellS(sst.add(risk.description())),
          cellN(risk.score()),
          cellS(sst.add(risk.level())),
          cellS(sst.add(risk.ownerName())))));
    }
    return sheetXml(rows.toString());
  }

  private static String sheetXml(String rowsXml) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
        + "<sheetData>" + rowsXml + "</sheetData></worksheet>";
  }

  private static String row(int rowIndex, List<String> cells) {
    StringBuilder sb = new StringBuilder();
    sb.append("<row r=\"").append(rowIndex).append("\">");
    for (int i = 0; i < cells.size(); i++) {
      sb.append(cells.get(i).replace("\"PLACEHOLDER\"", "\"" + colName(i + 1) + rowIndex + "\""));
    }
    sb.append("</row>");
    return sb.toString();
  }

  private static String cellS(int sstIndex) {
    return "<c r=\"PLACEHOLDER\" t=\"s\"><v>" + sstIndex + "</v></c>";
  }

  private static String cellN(long value) {
    return "<c r=\"PLACEHOLDER\"><v>" + value + "</v></c>";
  }

  private static String colName(int col) {
    return String.valueOf((char) ('A' + col - 1));
  }

  private static String xmlEsc(String value) {
    return value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
