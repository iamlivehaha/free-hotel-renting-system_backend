package com.example.backend.controller;

import com.example.backend.config.ProjectProperties;
import com.example.backend.model.Tenant;
import com.example.backend.model.TenantDao;
import com.example.backend.service.MailService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

@RestController
public class LoginRegisterController {

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private MailService mailService;

    @Autowired
    private ProjectProperties projectProperties;

    @GetMapping("/login")
    public Map<String, Object> login(HttpServletRequest request) {
        String email = request.getParameter("email").toString();
        String password = request.getParameter("password").toString();
        JSONObject response = new JSONObject();
        if (tenantDao.isValidEmail(email)) {
            response.put("ok", false);
            response.put("email", false);
            response.put("password", false);
            response.put("is_active", false);
            return response.toMap();
        } else {
            Tenant tmpTenant = tenantDao.getTenantByEmail(email);
            if (tmpTenant.getPassword().equals(password)) {
                response.put("email", true);
                response.put("password", true);
                if (tmpTenant.isActive()) {
                    response.put("ok", true);
                    response.put("is_active", true);
                    response.put("tenant_id", tmpTenant.getId());
                    response.put("name", tmpTenant.getName());
                } else {
                    response.put("ok", true);
                    response.put("is_active", false);
                }
                return response.toMap();
            } else {
                response.put("ok", false);
                response.put("email", true);
                response.put("password", false);
                response.put("is_active", false);
                return response.toMap();
            }
        }
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody String json) {
        JSONObject jsonObject = new JSONObject(json);
        String name = jsonObject.getString("name");
        String password = jsonObject.getString("password");
        String email = jsonObject.getString("email");
        JSONObject response = new JSONObject();
        if (tenantDao.isValidEmail(email)) {
            response.put("email", true);
            response.put("ok", true);
        } else {
            response.put("email", false);
            response.put("ok", false);
        }
        if (tenantDao.isValidName(name)) {
            response.put("name", true);
        } else {
            response.put("name", false);
            response.put("ok", false);
        }
        if (response.get("ok").toString().equals("true")) {
            Tenant tenant = new Tenant();
            tenant.setEmail(email);
            tenant.setName(name);
            tenant.setPassword(password);
            tenant.setAccountBalance("0.00");
            tenant.setActive(false);
            String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
            tenant.setActiveCode(uuid);
            tenantDao.saveTenant(tenant);
            Tenant tmpTenant = tenantDao.getTenantByName(name);
            String content = "??????????????????\n\n" + "\t?????????\n\n" + "\t???????????????????????????????????????????????????????????????Free??????????????????????????????\n\n\t"
                    + projectProperties.getBackendRootUrl() + "active_account?id=" + tmpTenant.getId() + "&active_code="
                    + uuid + "\n\n" + "\t?????????????????????????????????????????????????????????????????????????????????????????????\n\n" + "\t????????????????????????????????????????????????????????????????????????????????????";
            mailService.sendSimpleMail(email, "Free????????????????????????", content);
        }
        return response.toMap();
    }

    @GetMapping("/active_account")
    public String activeAccount(HttpServletRequest request) {
        String id = request.getParameter("id");
        String uuid = request.getParameter("active_code");
        Tenant tmpTenant = tenantDao.getTenantById(id);
        if (uuid.equals(tmpTenant.getActiveCode())) {
            tenantDao.activeTenant(id);
            return "??????????????????????????????";
        } else {
            return "????????????????????????????????????";
        }
    }
}
