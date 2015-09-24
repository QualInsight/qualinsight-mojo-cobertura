<?xml version="1.0" ?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output indent="yes" omit-xml-declaration="no" />
  <xsl:strip-space elements="*" />

  <xsl:variable name="TAB">
    <xsl:text>&#32;&#32;&#32;&#32;</xsl:text>
  </xsl:variable>
  <xsl:variable name="CR">
    <xsl:text>&#xA;</xsl:text>
  </xsl:variable>

  <xsl:key match="file" use="@path" name="path" />

  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="coverage">
    <xsl:value-of select="$CR" />
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates select="file[generate-id(.) = generate-id(key('path', @path))]" mode="sibling-recurse" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="file" mode="sibling-recurse">
    <xsl:value-of select="$CR" />
    <xsl:value-of select="$TAB" />
    <xsl:copy>
      <!-- back to default mode -->
      <xsl:apply-templates select="node() | @*" />
      <xsl:apply-templates select="following-sibling::file[@path = current()/@path]/node()" />
      <xsl:value-of select="$CR" />
      <xsl:value-of select="$TAB" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="lineToCover">
    <xsl:value-of select="$CR" />
    <xsl:value-of select="$TAB" />
    <xsl:value-of select="$TAB" />
    <xsl:copy>
      <xsl:copy-of select="@*" />
    </xsl:copy>
  </xsl:template>

</xsl:transform>