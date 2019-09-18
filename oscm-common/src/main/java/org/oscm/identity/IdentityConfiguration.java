package org.oscm.identity;

import lombok.Builder;
import lombok.Data;

@Builder(builderMethodName = "of")
@Data
public class IdentityConfiguration {

  String tenantId;
}
