# io.javaalmanac.sandbox - Java Sandboxes on javaalmanac.io

The sandboxes are based on OpenJDK Docker images for all versions since Java 8
and deployed as AWS Lambda functions.


## Security Warning

**The sandboxes allow arbitrary code execution. There are no built-in security
measures. Even the AWS access keys can be obtained from within the sandbox.**

Security is provided by the AWS Lambda execution environment. If you deploy the
sandboxes in your AWS environment please make sure to limit permissions to the
bare minimum (typically just logging) and limit the resource usage (e.g.
timeouts, network access).

## License

This code is provided "as is" under the [MIT License](LICENSE.md), without warranty of any kind.

## Trademarks

Java are registered trademarks of Oracle and/or its affiliates. Other names may be trademarks of their respective owners.
