<#-- @ftlvariable name="" type="com.chronopost.vision.microservices.healthcheck.SupervisionView" -->
<html>
    <body>
    	<body topmargin="5" leftmargin="5" rightmargin="0">
			<div style="font-size:18pt; font-family:verdana; font-weight:bold">
				
				<#assign statusAllService = "[OK]">
				<#list supervision?keys as key>
					<#assign bool = supervision[key].healthy>
					<#if bool = "false">
						<#assign statusAllService = "[KO]">
					</#if>
				</#list>
				SUPERVISION MICROSERVICES ${statusAllService}
			</div>
			<table width="100%" border="2" cellspacing="1" cellpadding="3">
				<tr>
					<b><td align="center" width="15%">Nom</b></td>
					<b><td align="center" width="30%">Description</b></td>
					<b><td align="center" width="5%">Status</b></td>
					<b><td align="center" width="25%">Texte</b></td>
					<b><td align="center" width="20%">Date</b></td>
				</tr>
			<#list supervision?keys as key>
				<#assign bool = supervision[key].healthy>
				<#if bool = "true">
					<tr>
						<td align="center" bgcolor="#00FF00" width="15%">${key}</td>
						<td align="center" bgcolor="#00FF00" width="35%">Test si reponse correcte</td>
						<td align="center" bgcolor="#00FF00" width="4%">[OK]</td>
						<td align="center" bgcolor="#00FF00" width="26%"></td>
						<td align="center" bgcolor="#00FF00" width="20%">${supervision[key].date?datetime}</td>						
					</tr>
				<#else>
					<tr>
						<td align="center" bgcolor="#FF7b00" width="15%">${key}</td>
						<td align="center" bgcolor="#FF7b00" width="35%">Test si reponse correcte</td>
						<td align="center" bgcolor="#FF7b00" width="4%">[KO]</td>
						<td align="center" bgcolor="#FF7b00" width="26%">${supervision[key].message}</td>
						<td align="center" bgcolor="#FF7b00" width="20%">${supervision[key].date?datetime}</td>
					</tr>					
				</#if>
				
		</#list>
		
		 
    </body>
</html>