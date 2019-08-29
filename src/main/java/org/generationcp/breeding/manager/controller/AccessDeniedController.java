
package org.generationcp.breeding.manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AccessDeniedController {

	@RequestMapping("/accessDenied")
	public String getAccessDeniedPage() {
		return "accessDenied";
	}
}
