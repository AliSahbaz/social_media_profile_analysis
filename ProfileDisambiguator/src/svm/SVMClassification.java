/**
 * Copyright (c) 2014 [National University of Singapore]
 * @Developer Karthick [karthyuom@gmail.com]
 */

package svm;

import java.io.File;
import java.io.IOException;

import util.ExcelDoc;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

/**
 * This class is responsible for svm training and classification. It uses libsvm
 * library for this purpose.
 * 
 * @param path
 *            - path to create svm model and deal with that.
 * @param modelName
 *            - name of the model.
 * @param param
 *            - svm configuration parameters such as kernel, gamma, svm type
 *            etc.
 */
public class SVMClassification {

    private String path;
    private String modelName;// svm_last_linear_80_dist.train
    private svm_parameter param;

    /**
     * Constructor that initializes class attributes.
     * 
     * @param path
     *            - path to svm model.
     * @param modelName
     *            - svm model name.
     */
    public SVMClassification(String path, String modelName) {
	this.path = path;
	this.modelName = modelName;
	param = new svm_parameter();
	param.probability = 1;
	param.gamma = 0.5;
	param.nu = 0.5;
	param.C = 1;
	param.svm_type = svm_parameter.C_SVC;
	param.kernel_type = svm_parameter.LINEAR;
	param.cache_size = 20000;
	param.eps = 0.001;
    }

    /**
     * This method can be used to create the model by training and then save the
     * model. the model saved will also be returned.
     * 
     * @param trainingFeatures
     *            - represents all the features derived from each training
     *            instances.
     * @return the svm_model as an object that is defined in the libsvm library
     *         itself.
     */
    public svm_model train(double trainingFeatures[][]) {
	svm_problem prob = new svm_problem();
	int dataCount = trainingFeatures.length;
	String pathToModel = "";

	String platform = System.getProperty("os.name");
	if (platform.contains("Windows")) {
	    pathToModel = path + "\\model\\";
	} else if (platform.contains("Mac")) {
	    pathToModel = path + "//model//";
	}

	File dir = new File(pathToModel);
	if (!dir.exists()) {
	    dir.getParentFile().mkdirs();
	}

	prob.y = new double[dataCount];
	prob.l = dataCount;
	prob.x = new svm_node[dataCount][];

	for (int i = 0; i < dataCount; i++) {
	    double[] features = trainingFeatures[i];
	    prob.x[i] = new svm_node[features.length - 1];
	    for (int j = 1; j < features.length; j++) {
		svm_node node = new svm_node();
		node.index = j;
		node.value = features[j];
		prob.x[i][j - 1] = node;
	    }
	    prob.y[i] = features[0];
	}

	svm_model model = svm.svm_train(prob, this.param);

	// save svm_model into a file
	try {
	    svm.svm_save_model(pathToModel + this.modelName, model);
	} catch (IOException ex) {
	    System.out.println("SVM model save exception: " + ex);
	}

	return model;
    }

    /**
     * This method gets the features that is derived from similarity measure
     * between each user and then by loading the model that is created already,
     * tells whether those users are same or not. Also, it logs the prediction
     * detail in an excel file.
     * 
     * @param userId1
     *            - represent the user name of a social network user.
     * @param userId2
     *            - represents the user name of another social network user.
     * @param excel
     *            - instance of ExcelDoc that is responsible for logging.
     * @param features
     *            - vector of similarity scores.
     * @return 1 or 0 as the output. 1 => matching 0 => non-matching.
     */
    public double predict(String userId1, String userId2, ExcelDoc excel,
	    double[] features) {
	try {
	    svm_model model;
	    svm_node[] nodes;
	    String pathToModel = "";

	    String platform = System.getProperty("os.name");
	    if (platform.contains("Windows")) {
		pathToModel = path + "\\model\\";
	    } else if (platform.contains("Mac")) {
		pathToModel = path + "//model//";
	    }

	    model = svm.svm_load_model(pathToModel + this.modelName);
	    nodes = new svm_node[features.length - 1];

	    for (int i = 1; i < features.length; i++) {
		svm_node node = new svm_node();
		node.index = i;
		node.value = features[i];
		nodes[i - 1] = node;
	    }

	    int totalClasses = 2;
	    int[] labels = new int[totalClasses];
	    svm.svm_get_labels(model, labels);

	    double[] prob_estimates = new double[totalClasses];
	    double v = svm
		    .svm_predict_probability(model, nodes, prob_estimates);

	    if (v == 1) {
		System.out.println("\nIDENTICAL USERS (Matching Percentage: "
			+ prob_estimates[0] * 100 + " % )");
		excel.createNextRowSVM(userId1, userId2, prob_estimates[0],
			prob_estimates[1], v);
	    } else {
		System.out
			.println("\nNON-IDENTICAL USERS (Matching Percentage: "
				+ prob_estimates[0] * 100 + " % )");
		excel.createNextRowSVM(userId1, userId2, prob_estimates[0],
			prob_estimates[1], v);
	    }

	    return v;
	} catch (IOException ex) {
	    System.out.println("SVM load exception: " + ex);
	}
	return -1;
    }

}
