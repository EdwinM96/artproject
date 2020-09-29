package com.art_project.controller;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.art_project.model.UserModel;
import com.art_project.model.result.ResultWrapper;
import com.art_project.security.JwtTokenUtil;
import com.art_project.service.PaymentService;
import com.art_project.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UserController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private UserService userService;

	@Autowired
	private PaymentService paymentService;

	private ResultWrapper<UserModel> result = new ResultWrapper<UserModel>();

	@RequestMapping
	public String redirect() {
		return "redirect:/login";
	}

	@GetMapping(value = "/login")
	public String loginGet(HttpServletRequest request, Model model, @ModelAttribute("userModel") UserModel userModel) {
		model.addAttribute("userModel", userModel);
		return "login";
	}

	@PostMapping(value = "/login")
	public String loginPost(HttpServletRequest request, Model model, @ModelAttribute("userModel") UserModel userModel) {

		HttpSession session = request.getSession();
		String token = getLoginToken(userModel);
		userModel = userService.findOne(userModel.getMobile());
		String paymentDone = userModel.getPaymentDone();

		if (token != null) {

			userService.setUserModel(userModel);

			if (paymentDone.equals("yes")) {
				session.setAttribute("userId", userModel.getId());
				System.out.println(" token: " + token);
				return "dashboard";
			} else {
				session.setAttribute("userId", userModel.getId());
				System.out.println("user id: " + userModel.getId());
				return "payment";
			}
		} else {
			request.setAttribute("loginStatus", "username or password incorrect.");
			System.out.println(" token: " + token);
			return "login";
		}
	}

	@GetMapping(value = "/sign-up")
	public String signUpGet(HttpServletRequest request, Model model, @ModelAttribute("userModel") UserModel userModel) {
		model.addAttribute("userModel", userModel);
		return "sign-up";
	}

	@PostMapping(value = "/sign-up")
	public String signUpPost(HttpServletRequest request, Model model,
			@ModelAttribute("userModel") UserModel userModel) {

		result = userService.saveUser(userModel);
		// request.setAttribute("registerStatus", "REGISTERED SUCCESSFULLY.");
		model.addAttribute(userModel);

		return "redirect:/login";
	}

	@RequestMapping(value = "/dashboard", method = { RequestMethod.GET, RequestMethod.POST })
	public String userWelcome(HttpServletRequest request) {
		return "dashboard";
	}

	public String getLoginToken(UserModel userModel) {
		Authentication authentication = null;
		String token = null;
		try {
			authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(userModel.getMobile(), userModel.getPassword()));
			System.out.println("authentication token: " + authentication);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			token = jwtTokenUtil.generateToken(authentication);
		} catch (Exception exception) {
			System.out.println("an error occured : " + exception.getMessage());
		}
		return token;
	}

}