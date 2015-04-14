package eu.amidst.core.learning;

import eu.amidst.core.datastream.DataInstance;
import eu.amidst.core.exponentialfamily.EF_LearningBayesianNetwork;
import eu.amidst.core.exponentialfamily.EF_UnivariateDistribution;
import eu.amidst.core.inference.VMP;
import eu.amidst.core.inference.vmp.Node;
import eu.amidst.core.variables.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by andresmasegosa on 10/03/15.
 */
public abstract class PlateuStructure {
    protected List<Node> parametersNode;
    protected List<List<Node>> plateuNodes;
    protected EF_LearningBayesianNetwork ef_learningmodel;
    protected int nRepetitions = 100;
    protected VMP vmp = new VMP();

    protected Map<Variable, Node> parametersToNode;

    protected List<Map<Variable, Node>> variablesToNode;

    public VMP getVMP() {
        return vmp;
    }

    public void resetQs() {
        this.vmp.resetQs();
    }

    public void setSeed(int seed) {
        this.vmp.setSeed(seed);
    }

    public EF_LearningBayesianNetwork getEFLearningBN() {
        return ef_learningmodel;
    }

    public void setNRepetitions(int nRepetitions_) {
        this.nRepetitions = nRepetitions_;
    }

    public void runInference() {
        this.vmp.runInference();
    }

    public double getLogProbabilityOfEvidence() {
        return this.vmp.getLogProbabilityOfEvidence();
    }

    public void setEFBayesianNetwork(EF_LearningBayesianNetwork model) {
        ef_learningmodel = model;
    }

    public Node getNodeOfVar(Variable variable, int slice) {
        if (variable.isParameterVariable())
            return this.parametersToNode.get(variable);
        else
            return this.variablesToNode.get(slice).get(variable);
    }

    public <E extends EF_UnivariateDistribution> E getEFParameterPosterior(Variable var) {
        if (!var.isParameterVariable())
            throw new IllegalArgumentException("Only parameter variables can be queried");

        return (E)this.parametersToNode.get(var).getQDist();
    }

    public <E extends EF_UnivariateDistribution> E getEFVariablePosterior(Variable var, int slice) {
        if (var.isParameterVariable())
            throw new IllegalArgumentException("Only non parameter variables can be queried");

        return (E) this.getNodeOfVar(var, slice).getQDist();
    }

    public abstract void replicateModel();

    public void setEvidence(List<DataInstance> data) {
        if (data.size()>nRepetitions)
            throw new IllegalArgumentException("The size of the data is bigger than the number of repetitions");

        for (int i = 0; i < nRepetitions && i<data.size(); i++) {
            final int slice = i;
            this.plateuNodes.get(i).forEach(node -> node.setAssignment(data.get(slice)));
        }

        for (int i = data.size(); i < nRepetitions; i++) {
            this.plateuNodes.get(i).forEach(node -> node.setAssignment(null));
        }
    }

}
