package cn.hehouhui.shandard;


import java.util.Optional;

/**
 * 签名模式
 *
 * @author HeHui
 * @date 2020-09-15 00:55
 */
public enum SignatureModel {

    /** MD5加密 */ MD5("MD5"),
    /** SHA-256 */ SHA256("SHA-256");

    public final String algorithm;

    SignatureModel(String algorithm) {
        this.algorithm = algorithm;
    }


    /**
     * 算法
     *
     * @param algorithm 算法
     *
     * @return {@link Optional}<{@link SignatureModel}>
     */
    public static Optional<SignatureModel> algorithmOf(String algorithm) {
        for (SignatureModel model : values()) {
            if (model.algorithm.equalsIgnoreCase(algorithm)) {
                return Optional.of(model);
            }
        }
        return Optional.empty();
    }
}
