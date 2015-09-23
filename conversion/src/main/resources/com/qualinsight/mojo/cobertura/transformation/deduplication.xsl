<?xml version="1.0" ?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output indent="yes" omit-xml-declaration="no" />

  <xsl:key match="file" use="@path" name="path" />

  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="coverage">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates select="file[generate-id(.) = generate-id(key('path', @path))]" mode="sibling-recurse" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="file" mode="sibling-recurse">
    <xsl:copy>
      <!-- back to default mode -->
      <xsl:apply-templates select="node() | @*" />
      <xsl:apply-templates select="following-sibling::file[@path = current()/@path]/node()" />
    </xsl:copy>
  </xsl:template>

</xsl:transform>