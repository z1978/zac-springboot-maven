package com.example.actor.web;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.actor.repository.Actor;
import com.example.actor.repository.ActorRepository;
import com.example.actor.repository.Prefecture;
import com.example.actor.repository.PrefectureRepository;
import com.example.actor.repository.SessionSample;

@Controller
public class ActorController {
	final static Logger logger = LoggerFactory.getLogger(ActorController.class);

	// セッションへ保存(方法１)
	// @Autowired
	// HttpSession dataSession;

	// セッションへ保存(方法２)
	@Autowired
	protected SessionSample sessionSample;

	@Autowired
	ActorRepository actorRepository;

	@Autowired
	PrefectureRepository prefectureRepository;

	@Autowired
	MessageSource msg;

	// サービスクラスがＤＩされる。
	@Autowired
	RandomNumberService random;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		sdf.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
	}

	@RequestMapping(value = "/actor", method = RequestMethod.GET)
	public String index(Model model) {
		logger.debug("Actor + index");
		List<Actor> list = actorRepository.findAll();
		if (CollectionUtils.isEmpty(list)) {
			String message = msg.getMessage("actor.list.empty", null, Locale.JAPAN);
			model.addAttribute("emptyMessage", message);
		}
		model.addAttribute("list", list);
		modelDump(model, "index");
		return "Actor/index";
	}

	@RequestMapping(value = "/actor/{id}", method = RequestMethod.GET)
	public ModelAndView detail(@PathVariable Integer id) {
		logger.debug("Actor + detail");

		// // セッションを取得(方法１)
		// ActorForm actorFormSession = (ActorForm) dataSession.getAttribute("form");
		// if (null != actorFormSession) {
		// // セッションよりデータを取得して設定
		// logger.debug("Birthday = " + actorFormSession.getBirthday());
		// }
		// // セッションを取得(方法１)(List)
		// TestSessionForms actorFormsSession = (TestSessionForms)
		// dataSession.getAttribute("forms");
		// if (null != actorFormsSession) {
		// // セッションよりデータを取得して設定
		// actorFormsSession.getActorForms().forEach(s -> logger.debug(s.getHeight() +
		// s.getBlood()));
		// // logger.debug("Birthday = " +
		// // actorFormsSession.getActorForms().get(0).getBirthday());
		// }
		// // セッションクリア
		// dataSession.invalidate();
		// セッションを取得(方法２)
		logger.debug("Name = " + sessionSample.getMsg());

		ModelAndView mv = new ModelAndView();
		mv.setViewName("Actor/detail");
		Actor actor = actorRepository.findOne(id);
		mv.addObject("actor", actor);
		return mv;
	}

	@RequestMapping(value = "/actor/search", method = RequestMethod.GET)
	public ModelAndView search(@RequestParam String keyword) {
		logger.debug("Actor + search");
		ModelAndView mv = new ModelAndView();
		mv.setViewName("Actor/index");
		if (StringUtils.isNotEmpty(keyword)) {
			List<Actor> list = actorRepository.findActors(keyword);
			if (CollectionUtils.isEmpty(list)) {
				String message = msg.getMessage("actor.list.empty", null, Locale.JAPAN);
				mv.addObject("emptyMessage", message);
			}
			mv.addObject("list", list);
		}
		return mv;
	}

	@RequestMapping(value = "/actor/create", method = RequestMethod.GET)
	public String create(ActorForm form, Model model) {
		logger.debug("Actor + create");
		
		Map<String, Integer> testRandom = random();
		logger.debug(testRandom.get("value").toString());
		
		List<Prefecture> pref = prefectureRepository.findAll();
		model.addAttribute("pref", pref);
		modelDump(model, "create");
		return "Actor/create";
	}

	@RequestMapping(value = "/actor/save", method = RequestMethod.POST)
	public String save(@Validated @ModelAttribute ActorForm form, BindingResult result, Model model) {
		logger.debug("Actor + save");

		// // セッションへ保存(方法１)
		// dataSession.setAttribute("form", form);
		// // セッションへ保存(方法１)(List)
		// List<ActorForm> actorForms = new ArrayList<ActorForm>();
		// actorForms.add(form);
		// ActorForm form2 = new ActorForm();
		// form2.setBirthday("1999-9-9");
		// form2.setBirthplaceId("22");
		// form2.setBlood("B");
		// form2.setHeight("99");
		// form2.setName("BBB");
		// actorForms.add(form2);
		// TestSessionForms testSessionForms = new TestSessionForms();
		// testSessionForms.setActorForms(actorForms);
		// dataSession.setAttribute("forms", testSessionForms);
		// セッションへ保存(方法２)
		StringBuilder sb = new StringBuilder();
		sb.append("セッションの受け渡しテスト用：入力したIDは");
		sb.append(form.getName());
		sb.append("です。");
		sessionSample.setMsg(sb.toString());
		logger.debug("Name = " + sessionSample.getMsg());

		if (result.hasErrors()) {
			String message = msg.getMessage("actor.validation.error", null, Locale.JAPAN);
			model.addAttribute("errorMessage", message);
			return create(form, model);
		}
		Actor actor = convert(form);
		logger.debug("actor:{}", actor.toString());
		actor = actorRepository.saveAndFlush(actor);
		modelDump(model, "save");
		return "redirect:/actor/" + actor.getId().toString();
	}

	@RequestMapping(value = "/actor/edit/{id}", method = RequestMethod.GET)
	public String edit(@PathVariable Integer id, ActorForm form, Model model) {
		logger.debug("Actor + edit");

		// セッションへ保存(方法２)
		StringBuilder sb = new StringBuilder();
		sb.append("セッションの受け渡しテスト用：入力したIDは");
		sb.append(id);
		sb.append("です。");
		sessionSample.setId(id);

		List<Prefecture> pref = prefectureRepository.findAll();
		model.addAttribute("pref", pref);
		Actor actor = actorRepository.findOne(id);
		form.setName(actor.getName());
		form.setHeight(actor.getHeight().toString());
		form.setBlood(actor.getBlood());
		form.setBirthday(actor.getBirthday().toString());
		form.setBirthplaceId(actor.getBirthplaceId().toString());
		model.addAttribute("actor", actor);
		modelDump(model, "edit");
		return "Actor/edit";
	}

	@RequestMapping(value = "/actor/update", method = RequestMethod.POST)
	public String update(@Validated @ModelAttribute ActorForm form, BindingResult result, Model model) {
		logger.debug("Actor + update");
		logger.debug("ID = " + sessionSample.getId());

		if (result.hasErrors()) {
			String message = msg.getMessage("actor.validation.error", null, Locale.JAPAN);
			model.addAttribute("errorMessage", message);
			return create(form, model);
		}
		Actor actor = convert(form);
		actor.setId(sessionSample.getId());
		logger.debug("actor:{}", actor.toString());
		actor = actorRepository.saveAndFlush(actor);
		modelDump(model, "save");
		return "redirect:/actor/" + actor.getId().toString();
	}

	@RequestMapping(value = "/actor/delete/{id}", method = RequestMethod.GET)
	public String delete(@PathVariable Integer id, RedirectAttributes attributes, Model model) {
		logger.debug("Actor + delete");
		actorRepository.delete(id);
		attributes.addFlashAttribute("deleteMessage", "delete ID:" + id);
		return "redirect:/actor";
	}

	/**
	 * convert form to model.
	 */
	private Actor convert(ActorForm form) {
		Actor actor = new Actor();
		actor.setName(form.getName());
		if (StringUtils.isNotEmpty(form.getHeight())) {
			actor.setHeight(Integer.valueOf(form.getHeight()));
		}
		if (StringUtils.isNotEmpty(form.getBlood())) {
			actor.setBlood(form.getBlood());
		}
		if (StringUtils.isNotEmpty(form.getBirthday())) {
			DateTimeFormatter withoutZone = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime parsed = LocalDateTime.parse(form.getBirthday() + " 00:00:00", withoutZone);
			Instant instant = parsed.toInstant(ZoneOffset.ofHours(9));
			actor.setBirthday(Date.from(instant));
		}
		if (StringUtils.isNotEmpty(form.getBirthplaceId())) {
			actor.setBirthplaceId(Integer.valueOf(form.getBirthplaceId()));
		}
		actor.setUpdateAt(new Date());
		return actor;
	}

	/**
	 * for debug.
	 */
	private void modelDump(Model model, String m) {
		logger.debug(" ");
		logger.debug("Model:{}", m);
		Map<String, Object> mm = model.asMap();
		for (Entry<String, Object> entry : mm.entrySet()) {
			logger.debug("key:{}, value:{}", entry.getKey(), entry.getValue().toString());
		}
	}

	// 乱数をレスポンスとして返却する。
	@RequestMapping("/random")
	public Map<String, Integer> random() {
		int value = random.zeroToNine();
		return Collections.singletonMap("value", value);
	}

}
