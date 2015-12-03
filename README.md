<h1>Logback Slack Appender</h1>

writes log output to Slack utilising the <a href="https://api.slack.com/incoming-webhooks">Webhooks API</a>

required parameters are:
<br/>level - threshold level for outout
<br/>pattern - logback pattern layout
<br/>endpoint - slack generated webhooks endpoint

Example configuration:

    <appender name="SLACK" class="org.dexr.logging.LogbackSlackAppender">
    <endpoint>"https://hooks.slack.com/services/<webhooks-token>"</endpoint>
    <level>ERROR</level>
      <layout class="ch.qos.logback.classic.PatternLayout">
          <pattern>%date %-5level - %logger{0} - %message%n</pattern>
      </layout>
    </appender>        
