package com.lilong.blog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.lilong.blog.base.Constants;
import com.lilong.blog.base.Result;
import com.lilong.blog.constants.RedisConstants;
import com.lilong.blog.constants.auth.SecurityConstants;
import com.lilong.blog.dto.Captcha;
import com.lilong.blog.dto.auth.AuthenticationToken;
import com.lilong.blog.dto.auth.EmailRegisterDto;
import com.lilong.blog.dto.auth.LoginDTO;
import com.lilong.blog.dto.user.LoginUserInfo;
import com.lilong.blog.enums.MenuTypeEnum;
import com.lilong.blog.exception.ServiceException;
import com.lilong.blog.helper.helper.CurrentUserHelper;
import com.lilong.blog.permission.TokenManager;
import com.lilong.blog.remote.system.QueryPermissionRequest;
import com.lilong.blog.remote.system.QueryRoleRequest;
import com.lilong.blog.service.AuthService;
import com.lilong.blog.utils.BeanCopyUtil;
import com.lilong.blog.utils.RedisUtil;
import com.lilong.blog.utils.SpringUtil;
import com.lilong.blog.vo.user.SysUserProfileVo;
import com.lilong.blogrpc.system.SysConfigServiceRpc;
import com.lilong.blogrpc.system.SysMenuServiceRpc;
import com.lilong.blogrpc.system.SysRoleServiceRpc;
import com.lilong.blogrpc.system.SysUserServiceRpc;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.zhyd.oauth.model.AuthCallback;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author : lilong
 * @date : 2026-02-07 22:46
 * @description :
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    /**
     * 网络图片地址
     **/
    private final static String IMG_URL = "https://v2.api-m.com/api/wallpaper?return=302";

    /**
     * 本地图片地址
     **/
    private final static String IMG_PATH = "G:\\work-plus\\blog-server\\blog-server\\resource\\%s.jpg";


    /**
     * 拼图验证码允许偏差
     **/
    private final static Integer ALLOW_DEVIATION = 3;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private SysConfigServiceRpc sysConfigServiceRpc;

    @Autowired
    private SysUserServiceRpc sysUserServiceRpc;

    @Autowired
    private SysRoleServiceRpc sysRoleServiceRpc;
    @Autowired
    private SysMenuServiceRpc sysMenuServiceRpc;

    @Override
    public AuthenticationToken login(LoginDTO loginDTO) {
        // 1. 创建用于密码认证的令牌（未认证）
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername().trim(), loginDTO.getPassword());
        // 2. 执行认证（认证中）
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. 认证成功后生成 JWT 令牌，并存入 Security 上下文，供登录日志 AOP 使用（已认证）
        AuthenticationToken authenticationTokenResponse =
                tokenManager.generateToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authenticationTokenResponse;
    }

    @Override
    public LoginUserInfo getLoginUserInfo(String source) {

        // 获取当前登录用户
        Result<SysUserProfileVo> currentUser = sysUserServiceRpc.selectCurrentUser();

        if (currentUser.isSuccess() && currentUser.getData() == null) {
            throw new RuntimeException("用户不存在");
        }

        LoginUserInfo loginUserInfo = BeanCopyUtil.copyObj(currentUser.getData().getSysUser(), LoginUserInfo.class);

        //获取菜单权限列表
        if (source.equalsIgnoreCase(Constants.ADMIN)) {
            List<String> permissions;

            Result<List<String>> roles = sysRoleServiceRpc.selectRoleCodesByUserId(
                    QueryRoleRequest.builder()
                            .userId(CurrentUserHelper.getUserId())
                            .build());

            if (roles.isSuccess() && roles.getData().contains(Constants.ADMIN)) {
                Result<List<String>> result = sysMenuServiceRpc.permissionList(QueryPermissionRequest.builder()
                        .userId(null)
                        .type(MenuTypeEnum.BUTTON.getCode()).build());
                permissions = result.getData();
            } else {
                Result<List<String>> result = sysMenuServiceRpc.permissionList(QueryPermissionRequest.builder()
                        .userId(CurrentUserHelper.getUserId())
                        .type(MenuTypeEnum.BUTTON.getCode()).build());
                permissions = result.getData();
            }
            loginUserInfo.setRoles(roles.getData());
            loginUserInfo.setPermissions(permissions);
        }

        return loginUserInfo;
    }

    @Override
    public Boolean sendEmailCode(String email) {
        return null;
    }

    @Override
    public Boolean register(EmailRegisterDto dto) {
        return null;
    }

    @Override
    public Boolean forgot(EmailRegisterDto dto) {
        return null;
    }

    @Override
    public String getWechatLoginCode() {
        return null;
    }

    @Override
    public LoginUserInfo getWechatIsLogin(String loginCode) {
        return null;
    }

    @Override
    public String wechatLogin(WxMpXmlMessage message) {
        return null;
    }

    @Override
    public String renderAuth(String source) {
        return null;
    }

    @Override
    public void authLogin(AuthCallback callback, String source, HttpServletResponse httpServletResponse) throws IOException {

    }

    @Override
    public LoginUserInfo appletLogin(String code) {
        return null;
    }

    /**
     * 获取滑块验证码
     *
     * @return
     */
    @Override
    public Captcha getCaptcha() {
        Captcha captcha = new Captcha();
        this.getCaptcha(captcha);
        return captcha;
    }

    @Override
    public void logout() {
        String token = CurrentUserHelper.getTokenFromRequest();
        forceLogout(token);
    }

    @Override
    public void forceLogout(String token) {
        if (StrUtil.isNotBlank(token) && token.startsWith(SecurityConstants.BEARER_TOKEN_PREFIX)) {
            token = token.substring(SecurityConstants.BEARER_TOKEN_PREFIX.length());
            // 将JWT令牌加入黑名单
            tokenManager.invalidateToken(token);
            // 清除Security上下文
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * 校验验证码
     *
     * @param imageKey
     * @param imageCode
     * @return boolean
     **/
    public static void checkImageCode(String imageKey, String imageCode) {
        RedisUtil redisUtil = SpringUtil.getBean(RedisUtil.class);
        Object text = redisUtil.get(RedisConstants.SLIDER_CAPTCHA_CODE_KEY + imageKey);
        if (Objects.isNull(text)) {
            throw new ServiceException("验证码已失效");
        }
        // 根据移动距离判断验证是否成功
        if (Math.abs(Integer.parseInt(text.toString()) - Integer.parseInt(imageCode)) > ALLOW_DEVIATION) {
            throw new ServiceException("验证失败，请控制拼图对齐缺口");
        }
    }

    /**
     * 获取图片资源
     *
     * @param captcha
     * @return
     */
    private void getCaptcha(Captcha captcha) {
        //参数校验
        checkCaptcha(captcha);
        //获取画布的宽高
        int canvasWidth = captcha.getCanvasWidth();
        int canvasHeight = captcha.getCanvasHeight();
        //获取阻塞块的宽高/半径
        int blockWidth = captcha.getBlockWidth();
        int blockHeight = captcha.getBlockHeight();
        int blockRadius = captcha.getBlockRadius();
        //获取资源图
        BufferedImage canvasImage = getBufferedImage(captcha.getPlace());
        //调整原图到指定大小
        canvasImage = imageResize(canvasImage, canvasWidth, canvasHeight);
        //随机生成阻塞块坐标
        int blockX = getNonceByRange(blockWidth, canvasWidth - blockWidth - 10);
        int blockY = getNonceByRange(10, canvasHeight - blockHeight + 1);
        //阻塞块
        BufferedImage blockImage = new BufferedImage(blockWidth, blockHeight, BufferedImage.TYPE_4BYTE_ABGR);
        //新建的图像根据轮廓图颜色赋值，源图生成遮罩
        cutByTemplate(canvasImage, blockImage, blockWidth, blockHeight, blockRadius, blockX, blockY);
        // 移动横坐标
        String nonceStr = UUID.randomUUID().toString().replaceAll("-", "");
        // 缓存
        saveImageCode(nonceStr, String.valueOf(blockX));
        //设置返回参数
        captcha.setNonceStr(nonceStr);
        captcha.setBlockY(blockY);
        captcha.setBlockSrc(toBase64(blockImage, "png"));
        captcha.setCanvasSrc(toBase64(canvasImage, "png"));
    }

    /**
     * BufferedImage转BASE64
     */
    private String toBase64(BufferedImage bufferedImage, String type) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, type, byteArrayOutputStream);
            String base64 = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
            return String.format("data:image/%s;base64,%s", type, base64);
        } catch (IOException e) {
            System.out.println("图片资源转换BASE64失败");
            //异常处理
            return null;
        }
    }


    /**
     * 缓存验证码，有效期1分钟
     *
     * @param key
     * @param code
     **/
    private void saveImageCode(String key, String code) {
        RedisUtil redisUtil = SpringUtil.getBean(RedisUtil.class);
        redisUtil.set(RedisConstants.SLIDER_CAPTCHA_CODE_KEY + key, code, RedisConstants.MINUTE_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     * 抠图，并生成阻塞块
     **/
    private void cutByTemplate(BufferedImage canvasImage, BufferedImage blockImage, int blockWidth, int blockHeight, int blockRadius, int blockX, int blockY) {
        BufferedImage waterImage = new BufferedImage(blockWidth, blockHeight, BufferedImage.TYPE_4BYTE_ABGR);
        //阻塞块的轮廓图
        int[][] blockData = getBlockData(blockWidth, blockHeight, blockRadius);
        //创建阻塞块具体形状
        for (int i = 0; i < blockWidth; i++) {
            for (int j = 0; j < blockHeight; j++) {
                try {
                    //原图中对应位置变色处理
                    if (blockData[i][j] == 1) {
                        //背景设置为黑色
                        waterImage.setRGB(i, j, Color.BLACK.getRGB());
                        blockImage.setRGB(i, j, canvasImage.getRGB(blockX + i, blockY + j));
                        //轮廓设置为白色，取带像素和无像素的界点，判断该点是不是临界轮廓点
                        if (blockData[i + 1][j] == 0 || blockData[i][j + 1] == 0 || blockData[i - 1][j] == 0 || blockData[i][j - 1] == 0) {
                            blockImage.setRGB(i, j, Color.WHITE.getRGB());
                            waterImage.setRGB(i, j, Color.WHITE.getRGB());
                        }
                    }
                    //这里把背景设为透明
                    else {
                        blockImage.setRGB(i, j, Color.TRANSLUCENT);
                        waterImage.setRGB(i, j, Color.TRANSLUCENT);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    //防止数组下标越界异常
                }
            }
        }
        //在画布上添加阻塞块水印
        addBlockWatermark(canvasImage, waterImage, blockX, blockY);
    }

    /**
     * 构建拼图轮廓轨迹
     **/
    private int[][] getBlockData(int blockWidth, int blockHeight, int blockRadius) {
        int[][] data = new int[blockWidth][blockHeight];
        double po = Math.pow(blockRadius, 2);
        //随机生成两个圆的坐标，在4个方向上 随机找到2个方向添加凸/凹
        //凸/凹1
        int face1 = RandomUtils.nextInt(0, 4);
        //凸/凹2
        int face2;
        //保证两个凸/凹不在同一位置
        do {
            face2 = RandomUtils.nextInt(0, 4);
        } while (face1 == face2);
        //获取凸/凹起位置坐标
        int[] circle1 = getCircleCoords(face1, blockWidth, blockHeight, blockRadius);
        int[] circle2 = getCircleCoords(face2, blockWidth, blockHeight, blockRadius);
        //随机凸/凹类型
        int shape = getNonceByRange(0, 1);
        //圆的标准方程 (x-a)²+(y-b)²=r²,标识圆心（a,b）,半径为r的圆
        //计算需要的小图轮廓，用二维数组来表示，二维数组有两张值，0和1，其中0表示没有颜色，1有颜色
        for (int i = 0; i < blockWidth; i++) {
            for (int j = 0; j < blockHeight; j++) {
                data[i][j] = 0;
                //创建中间的方形区域
                if ((i >= blockRadius && i <= blockWidth - blockRadius && j >= blockRadius && j <= blockHeight - blockRadius)) {
                    data[i][j] = 1;
                }
                double d1 = Math.pow(i - Objects.requireNonNull(circle1)[0], 2) + Math.pow(j - circle1[1], 2);
                double d2 = Math.pow(i - Objects.requireNonNull(circle2)[0], 2) + Math.pow(j - circle2[1], 2);
                //创建两个凸/凹
                if (d1 <= po || d2 <= po) {
                    data[i][j] = shape;
                }
            }
        }
        return data;
    }

    /**
     * 在画布上添加阻塞块水印
     */
    private static void addBlockWatermark(BufferedImage canvasImage, BufferedImage blockImage, int x, int y) {
        Graphics2D graphics2D = canvasImage.createGraphics();
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.8f));
        graphics2D.drawImage(blockImage, x, y, null);
        graphics2D.dispose();
    }

    /**
     * 根据朝向获取圆心坐标
     */
    private static int[] getCircleCoords(int face, int blockWidth, int blockHeight, int blockRadius) {
        //上
        if (0 == face) {
            return new int[]{blockWidth / 2 - 1, blockRadius};
        }
        //左
        else if (1 == face) {
            return new int[]{blockRadius, blockHeight / 2 - 1};
        }
        //下
        else if (2 == face) {
            return new int[]{blockWidth / 2 - 1, blockHeight - blockRadius - 1};
        }
        //右
        else if (3 == face) {
            return new int[]{blockWidth - blockRadius - 1, blockHeight / 2 - 1};
        }
        return null;
    }

    /**
     * 调整图片大小
     **/
    public static BufferedImage imageResize(BufferedImage bufferedImage, int width, int height) {
        Image image = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = resultImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose();
        return resultImage;
    }

    /**
     * 获取指定范围内的随机数
     **/
    public static int getNonceByRange(int start, int end) {
        Random random = new Random();
        return random.nextInt(end - start + 1) + start;
    }

    /**
     * 获取验证码资源图
     **/
    public BufferedImage getBufferedImage(Integer place) {
        try {
            //随机图片
            int nonce = getNonceByRange(0, 1000);
            //获取网络资源图片
            if (0 == place) {
//                String imgUrl = String.format(IMG_URL, nonce);
                URL url = new URL(IMG_URL);
                return ImageIO.read(url.openStream());
            }
            //获取本地图片
            else {
                // TODO先写死
                String imgPath = String.format(IMG_PATH, 950);
                File file = new File(imgPath);
                return ImageIO.read(file);
            }
        } catch (Exception e) {
            System.out.println("获取拼图资源失败");
            //异常处理
            return null;
        }
    }

    /**
     * 入参校验设置默认值
     **/
    private static void checkCaptcha(Captcha captcha) {
        //设置画布宽度默认值
        if (captcha.getCanvasWidth() == null) {
            captcha.setCanvasWidth(320);
        }
        //设置画布高度默认值
        if (captcha.getCanvasHeight() == null) {
            captcha.setCanvasHeight(155);
        }
        //设置阻塞块宽度默认值
        if (captcha.getBlockWidth() == null) {
            captcha.setBlockWidth(65);
        }
        //设置阻塞块高度默认值
        if (captcha.getBlockHeight() == null) {
            captcha.setBlockHeight(55);
        }
        //设置阻塞块凹凸半径默认值
        if (captcha.getBlockRadius() == null) {
            captcha.setBlockRadius(9);
        }
        //设置图片来源默认值
        if (captcha.getPlace() == null) {
            captcha.setPlace(1);
        }
    }
}
