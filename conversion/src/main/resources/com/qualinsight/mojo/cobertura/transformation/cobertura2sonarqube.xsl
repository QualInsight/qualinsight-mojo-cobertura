<?xml version="1.0" ?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output indent="yes" omit-xml-declaration="no" />

  <xsl:variable name="TAB">
    <xsl:text>&#32;&#32;&#32;&#32;</xsl:text>
  </xsl:variable>
  <xsl:variable name="CR">
    <xsl:text>&#xA;</xsl:text>
  </xsl:variable>
  <xsl:param name="SRC_DIR"/>

  <xsl:template match="/coverage">
    <xsl:value-of select="$CR" />
    <coverage version="1">
      <xsl:value-of select="$CR" />
      <xsl:apply-templates mode="custom-copy" select="." />
    </coverage>
  </xsl:template>

  <xsl:template mode="custom-copy" match="/coverage/packages/package/classes">
    <xsl:for-each select="class">
      <xsl:variable name="currFilename" select="@filename" />
      <xsl:if test=". = /coverage/packages/package/classes/class[@filename=$currFilename][1]">
        <xsl:value-of select="$TAB" />
        <file path="{$SRC_DIR}{@filename}">
          <xsl:value-of select="$CR" />
          <xsl:for-each select="/coverage/packages/package/classes/class[@filename=$currFilename]">
            <xsl:for-each select="lines/line">
              <xsl:apply-templates mode="custom-copy" select="." />
            </xsl:for-each>
          </xsl:for-each>
          <xsl:value-of select="$TAB" />
        </file>
        <xsl:value-of select="$CR" />
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="custom-copy" match="/coverage/packages/package/classes/class/lines/line">
    <xsl:value-of select="$TAB" />
    <xsl:value-of select="$TAB" />
    <xsl:choose>
      <xsl:when test="@condition-coverage">
        <xsl:variable name="COVERAGE_SEPARATOR"><![CDATA[/]]></xsl:variable>
        <xsl:variable name="COVERAGE" select="translate(translate(substring-after(normalize-space(@condition-coverage), '% '), ')', ''), '(', '')" />
        <lineToCover lineNumber="{@number}" covered="{boolean(@hits &gt; 0)}" branchesToCover="{substring-after($COVERAGE, $COVERAGE_SEPARATOR)}" coveredBranches="{substring-before($COVERAGE, $COVERAGE_SEPARATOR)}" />
      </xsl:when>
      <xsl:otherwise>
        <lineToCover lineNumber="{@number}" covered="{boolean(@hits &gt; 0)}" />
      </xsl:otherwise>
    </xsl:choose>
    <xsl:value-of select="$CR" />
  </xsl:template>

  <xsl:template mode="custom-copy" match="@* | node()">
    <xsl:apply-templates mode="custom-copy" select="@* | node()" />
  </xsl:template>
</xsl:transform>