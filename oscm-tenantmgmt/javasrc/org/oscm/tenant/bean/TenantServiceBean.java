/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2016
 *
 *  Creation Date: 30.08.2016
 *
 *******************************************************************************/
package org.oscm.tenant.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
import javax.interceptor.Interceptors;

import org.oscm.domobjects.Tenant;
import org.oscm.domobjects.TenantSetting;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.TenantService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOTenant;
import org.oscm.internal.vo.VOTenantSetting;
import org.oscm.tenant.assembler.TenantAssembler;
import org.oscm.tenant.local.TenantServiceLocal;

import java.util.ArrayList;
import java.util.List;

@Stateless
@Remote(TenantService.class)
@RolesAllowed("PLATFORM_OPERATOR")
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class TenantServiceBean implements TenantService {

    @EJB
    TenantServiceLocal tenantServiceLocal;

    @Override
    public List<VOTenant> getTenants() {
        List<VOTenant> voTenants = new ArrayList<>();
        for (Tenant tenant : tenantServiceLocal.getAllTenants()) {
            voTenants.add(TenantAssembler.toVOTenant(tenant));
        }
        return voTenants;
    }

    @Override
    public VOTenant getTenantByTenantId(String tenantId) throws ObjectNotFoundException {
        return TenantAssembler.toVOTenant(tenantServiceLocal.getTenantByTenantId(tenantId));
    }

    @Override
    public void addTenant(VOTenant voTenant) throws NonUniqueBusinessKeyException {
        tenantServiceLocal.saveTenant(TenantAssembler.toTenant(voTenant));
    }

    @Override
    public void updateTenant(VOTenant voTenant)
        throws NonUniqueBusinessKeyException, ObjectNotFoundException, ConcurrentModificationException {
        Tenant tenantToUpdate = tenantServiceLocal.getTenantByKey(voTenant.getKey());
        tenantServiceLocal.saveTenant(TenantAssembler.updateTenantData(voTenant,
            tenantToUpdate));
    }

    @Override
    public void removeTenant(VOTenant voTenant) throws ObjectNotFoundException {
        Tenant tenantToRemove = tenantServiceLocal.getTenantByKey(voTenant.getKey());
        tenantServiceLocal.removeTenant(tenantToRemove);
    }

    @Override
    public void addTenantSettings(List<VOTenantSetting> tenantSettings, VOTenant voTenant) throws
        NonUniqueBusinessKeyException, ObjectNotFoundException {
        removeTenantIdpProperties(voTenant);
        for (VOTenantSetting voTenantSetting : tenantSettings) {
            tenantServiceLocal.saveTenantSetting(TenantAssembler.toTenantSetting(voTenantSetting));
        }
    }

    @Override
    public void removeTenantIdpProperties(VOTenant voTenant) throws ObjectNotFoundException {
        Tenant tenant = new Tenant();
        tenant.setKey(voTenant.getKey());
        List<TenantSetting> settings = tenantServiceLocal.getAllTenantSettingsForTenant(tenant);
        if (settings.isEmpty()) {
            return;
        }
        for (TenantSetting tenantSetting : settings) {
            tenantServiceLocal.removeTenantSetting(tenantSetting);
        }
    }

    @Override
    public List<VOTenantSetting> getSettingsForTenant(long key) {
        Tenant tenant = new Tenant();
        tenant.setKey(key);
        List<VOTenantSetting> voTenantSettings = new ArrayList<>();
        List<TenantSetting> settings =  tenantServiceLocal.getAllTenantSettingsForTenant(tenant);
        for (TenantSetting tenantSetting : settings) {
            voTenantSettings.add(TenantAssembler.toVOTenantSetting(tenantSetting));
        }
        return voTenantSettings;
    }
}
