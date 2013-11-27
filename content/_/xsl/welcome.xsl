<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" encoding="utf-8" indent="yes"/>

    <xsl:template match="/root">
        <html>
            <head>
                <title>Welcome page</title>
            </head>
            <body>
                id:
                <xsl:value-of select="product/id"/>
                <br/>
                <xsl:for-each select="products/product">
                    <h1>
                        <xsl:value-of select="id"/>
                    </h1>
                    <h3>
                        <xsl:value-of select="serialNumber"/>
                    </h3>
                </xsl:for-each>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>