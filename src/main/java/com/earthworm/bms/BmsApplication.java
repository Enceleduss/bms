package com.earthworm.bms;

import com.earthworm.bms.model.CustomerRecord;
import com.earthworm.bms.model.Folder;
import com.earthworm.bms.model.Role;
import com.earthworm.bms.repository.CustomerRepository;
import com.earthworm.bms.repository.FolderRepository;
import com.earthworm.bms.repository.RoleRepository;
import com.earthworm.bms.service.GraphNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class BmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BmsApplication.class, args);
	}
	@Bean
	CommandLineRunner run(RoleRepository roleRepository,
						  CustomerRepository userRepository,
						  PasswordEncoder passwordEncode,
						  FolderRepository folderRepository,
						  GraphNodeService _g){
		return args ->{
			System.out.println("folder not found");
			if(_g.getCompanyPublicFolder().isEmpty())
			{
				System.out.println("folder not found");

				Folder companyPublicFolder = _g.createCompanyPublicFolder();
				folderRepository.save(companyPublicFolder);
			}
			if(roleRepository.findByAuthority("ADMIN").isPresent()) return;
			Role adminRole = roleRepository.save(new Role("ADMIN"));
			roleRepository.save(new Role("USER"));

			Set<Role> roles = new HashSet<>();
			roles.add(adminRole);

			CustomerRecord admin = new CustomerRecord(1, "admin", passwordEncode.encode("password"), roles);

			userRepository.save(admin);

		};
	}
}
