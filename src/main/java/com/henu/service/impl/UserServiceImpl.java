package com.henu.service.impl;

import com.henu.code.Code;
import com.henu.entity.Article;
import com.henu.entity.Tag;
import com.henu.entity.User;
import com.henu.entity.UserInfo;
import com.henu.repository.ArticleRepository;
import com.henu.repository.TagRepository;
import com.henu.repository.UserInfoRepository;
import com.henu.repository.UserRepository;
import com.henu.service.UserService;
import com.henu.utils.BlogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private UserInfoRepository userInfoRepository;

    @Override
    public List<User> findALl() {
        return userRepository.findAll();
    }

    @Override
    public User findByID(int id) {
        return userRepository.findById(id);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public int insertUser(User user) {
        String email = user.getEmail();
        User byEmail = userRepository.findByEmail(email);
        if (byEmail != null && email.equals(byEmail.getEmail())) {
            return 20;
        }
        String userName = user.getUserName();
        User byName = userRepository.findByName(userName);
        if (byName != null && userName.equals(byName.getUserName())) {
            return 21;
        }

        return userRepository.insertUser(user);
    }

    @Override
    public User login(String email) {
        List<User> all = userRepository.findAll();
        User user = null;
        for (User userOfOne :
                all) {
            if (userOfOne.getEmail().equals(email)) {
                user = userOfOne;
            }
        }
        return user;
    }

    @Override
    @Transactional
    public int register(User user) {
        User byEmail = userRepository.findByEmail(user.getEmail());
        if (byEmail != null) {
            return Code.EMAIL_EXITS;
        }
        int i = userRepository.insertUser(user);
        if (i > 0) {
            return Code.REGISTER_SUCCESS;
        }
        return Code.REGISTER_ERROR;
    }

    //用户的所有文章
    @Override
    @Transactional
    public List<Article> getUserArticles(int id) {
        //通过用户id查询用户发布的文章
        List<Article> userArticles = articleRepository.getUserArticles(id);
        //查询文章的所有tag
        for (Article article :
                userArticles) {
            List<Tag> articleTagsById = tagRepository.getArticleTagsById(article.getId());
            article.setTags(articleTagsById);
        }
        return userArticles;
    }

    //友链
    @Override
    public UserInfo getUserInfoById(int id) {
        return userInfoRepository.getUserInfoById(id);
    }

    //获取用户信息 文章数，标签数，友链
    @Override
    @Transactional
    public Map<String, Object> getUserInfo(int id) {
        Map<String, Object> map = new HashMap<>();
        User user = findByID(id);
        if (user == null) {
            return null;
        }

        map.put("userName", user.getUserName());
        UserInfo userInfoById = getUserInfoById(id);
        System.out.println("userinfo    findById" + userInfoById);
        if(userInfoById==null) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUid(id);
            System.out.println("userinfo    findById" + userInfoById);
            map.put("userInfo", userInfo);
        } else {
            map.put("userInfo", userInfoById);
        }

        List<Article> userArticles = getUserArticles(id);
        if (userArticles == null) {
            map.put("userArticles", 0);
            map.put("tags", 0);
            return map;
        }

        map.put("userArticles", userArticles.size());
        List<Tag> tags = tagRepository.getUserTags(id);
        if (tags != null) {
            map.put("tags", tags.size());
        }

        return map;
    }

    @Override
    @Transactional
    public int updateUser(User user) {
        return userRepository.updateUser(user);
    }

    @Override
    @Transactional
    public int updateUserInfo(UserInfo userInfo) {
        if(userInfoRepository.isUserInfoExist(userInfo.getUid()) == null){
            return userInfoRepository.addInfo(userInfo);
        }
        return userInfoRepository.updateUserInfo(userInfo);
    }
}
