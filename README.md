Logback Slack Appender

writes log output to Slack utilising the Webhooks API

required parameters are:

threshold level

pattern

endpoint

Example configuration:

    <appender name="SLACK" class="org.dexr.logging.LogbackSlackAppender">
    <endpoint>"https://hooks.slack.com/services/<webhooks-token>"</endpoint>
    <level>ERROR</level>
      <layout class="ch.qos.logback.classic.PatternLayout">
          <pattern>${runningNode} %date %-5level - %logger{0} - %message%n</pattern>
      </layout>
    </appender>        
