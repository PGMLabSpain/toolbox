package eu.amidst.huginlink.converters;

import COM.hugin.HAPI.*;
import eu.amidst.core.datastream.filereaders.arffFileReader.ARFFDataReader;
import eu.amidst.core.distribution.*;
import eu.amidst.core.models.BayesianNetwork;
import eu.amidst.core.models.DAG;
import eu.amidst.core.models.ParentSet;
import eu.amidst.core.utils.MultinomialIndex;
import eu.amidst.core.utils.Utils;
import eu.amidst.core.variables.StaticVariables;
import eu.amidst.core.variables.Variable;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by afa on 18/11/14.
 */
public class BNConverterToHuginTest {

    private BayesianNetwork amidstBN;
    private Domain huginBN;

    public static BayesianNetwork getAmidstBayesianNetworkExample(){

        //**************************************** Synthetic data ******************************************************

        ARFFDataReader fileReader = new ARFFDataReader();
        fileReader.loadFromFile("datasets/syntheticData.arff");
        StaticVariables modelHeader = new StaticVariables(fileReader.getAttributes());


        //***************************************** Network structure **************************************************
        //Create the structure by hand

        DAG dag = new DAG(modelHeader);
        StaticVariables variables = dag.getStaticVariables();

        Variable A, B, C, D, E, G, H, I;

        A = variables.getVariableById(0);
        B = variables.getVariableById(1);
        C = variables.getVariableById(2);
        D = variables.getVariableById(3);
        E = variables.getVariableById(4);
        G = variables.getVariableById(5);
        H = variables.getVariableById(6);
        I = variables.getVariableById(7);

        //Example

        dag.getParentSet(E).addParent(A);
        dag.getParentSet(E).addParent(B);

        dag.getParentSet(H).addParent(A);
        dag.getParentSet(H).addParent(B);

        dag.getParentSet(I).addParent(A);
        dag.getParentSet(I).addParent(B);
        dag.getParentSet(I).addParent(C);
        dag.getParentSet(I).addParent(D);

        dag.getParentSet(G).addParent(C);
        dag.getParentSet(G).addParent(D);

        BayesianNetwork bn = BayesianNetwork.newBayesianNetwork(dag);

        //****************************************** Distributions *****************************************************

        /* IMPORTANT: The parents are indexed according to Koller (Chapter 10. Pag. 358). Example:
           Parents: A = {A0,A1} and B = {B0,B1,B2}.
           NumberOfPossibleAssignments = 6

           Index   A    B
             0     A0   B0
             1     A1   B1
             2     A0   B2
             3     A1   B0
             4     A0   B1
             5     A1   B2
        */



        // Variable A
        Multinomial distA = bn.getDistribution(A);
        distA.setProbabilities(new double[]{0.3,0.7});

        // Variable B
        Multinomial distB = bn.getDistribution(B);
        distB.setProbabilities(new double[]{0.4,0.1,0.5});

        // Variable C
        Normal distC = bn.getDistribution(C);
        distC.setMean(0.8);
        distC.setSd(1.5);

        // Variable D
        Normal distD = bn.getDistribution(D);
        distD.setMean(1.3);
        distD.setSd(0.9);

        // Variable E
        Multinomial_MultinomialParents distE=bn.getDistribution(E);
        distE.getMultinomial(0).setProbabilities(new double[]{0.2,0.8});
        distE.getMultinomial(1).setProbabilities(new double[]{0.1,0.9});
        distE.getMultinomial(2).setProbabilities(new double[]{0.8,0.2});
        distE.getMultinomial(3).setProbabilities(new double[]{0.45,0.55});
        distE.getMultinomial(4).setProbabilities(new double[]{0.35,0.65});
        distE.getMultinomial(5).setProbabilities(new double[]{0.9,0.1});

        // Variable H
        Normal_MultinomialParents distH = bn.getDistribution(H);
        distH.getNormal(0).setMean(2);
        distH.getNormal(0).setSd(1.5);
        distH.getNormal(1).setMean(-1);
        distH.getNormal(1).setSd(0.5);
        distH.getNormal(2).setMean(3);
        distH.getNormal(2).setSd(0.8);
        distH.getNormal(3).setMean(2);
        distH.getNormal(3).setSd(1);
        distH.getNormal(4).setMean(5);
        distH.getNormal(4).setSd(0.8);
        distH.getNormal(5).setMean(1.5);
        distH.getNormal(5).setSd(0.7);

        //Variable I
        Normal_MultinomialNormalParents distI = bn.getDistribution(I);
        distI.getConditionalLinearGaussianDistribution(0).setBeta0(0.5);
        distI.getConditionalLinearGaussianDistribution(0).setBetaForParent(C, 0.25);
        distI.getConditionalLinearGaussianDistribution(0).setBetaForParent(C, 0.4);
        distI.getConditionalLinearGaussianDistribution(0).setSd(0.9);

        distI.getConditionalLinearGaussianDistribution(1).setBeta0(-0.1);
        distI.getConditionalLinearGaussianDistribution(1).setBetaForParent(C, -0.5);
        distI.getConditionalLinearGaussianDistribution(1).setBetaForParent(C, 0.2);
        distI.getConditionalLinearGaussianDistribution(1).setSd(0.6);

        distI.getConditionalLinearGaussianDistribution(2).setBeta0(2.1);
        distI.getConditionalLinearGaussianDistribution(2).setBetaForParent(C, 1.2);
        distI.getConditionalLinearGaussianDistribution(2).setBetaForParent(C, -0.3);
        distI.getConditionalLinearGaussianDistribution(2).setSd(1.1);

        distI.getConditionalLinearGaussianDistribution(3).setBeta0(2.1);
        distI.getConditionalLinearGaussianDistribution(3).setBetaForParent(C, 1.25);
        distI.getConditionalLinearGaussianDistribution(3).setBetaForParent(C, 0.9);
        distI.getConditionalLinearGaussianDistribution(3).setSd(0.95);

        distI.getConditionalLinearGaussianDistribution(4).setBeta0(1.5);
        distI.getConditionalLinearGaussianDistribution(4).setBetaForParent(C, -0.41);
        distI.getConditionalLinearGaussianDistribution(4).setBetaForParent(C, 0.5);
        distI.getConditionalLinearGaussianDistribution(4).setSd(1.5);

        distI.getConditionalLinearGaussianDistribution(5).setBeta0(0);
        distI.getConditionalLinearGaussianDistribution(5).setBetaForParent(C, 0.0);
        distI.getConditionalLinearGaussianDistribution(5).setBetaForParent(C, 0.3);
        distI.getConditionalLinearGaussianDistribution(5).setSd(0.25);

        //Variable G
        ConditionalLinearGaussian distG  = bn.getDistribution(G);
        distG.setBeta0(0.7);
        distG.setBetaForParent(C, 0.3);
        distG.setBetaForParent(D, -0.8);
        distG.setSd(0.9);


        return bn;


    }

    @Before
    public void setUp() throws ExceptionHugin {

        //AMIDST Bayesian network built by hand. Update the attribute amidstBN used next for the tests.
        this.amidstBN = getAmidstBayesianNetworkExample();

        //--------------------------------------------------------------------------------------------------------------
        //Conversion from AMIDST network into a Hugin network.
        System.out.println("\n\nConverting the AMIDST network into Hugin format ...");
        this.huginBN = BNConverterToHugin.convertToHugin(amidstBN);
        String outFile = new String("networks/huginNetworkFromAMIDST.net");
        this.huginBN.saveAsNet(new String(outFile));
        System.out.println("Hugin network saved in \"" + outFile + "\"" + ".");

        //--------------------------------------------------------------------------------------------------------------

        ParseListener parseListener2 = new DefaultClassParseListener();
        this.huginBN = new Domain (outFile, parseListener2);
        System.out.println("\n\nConverting the previous Hugin network into AMIDST format ...");
        this.amidstBN = BNConverterToAMIDST.convertToAmidst(this.huginBN);
        System.out.println("\nAMIDST network object created.");

        //--------------------------------------------------------------------------------------------------------------

        System.out.println("\n\nConverting the previous AMIDST network into Hugin format ...");
        Domain huginNetwork2 = BNConverterToHugin.convertToHugin(amidstBN);
        String outFile2 = new String("networks/huginNetworkFromAMIDST2.net");
        huginNetwork2.saveAsNet(new String(outFile2));
        System.out.println("Hugin network saved in \"" + outFile2 + "\"" + ".");

        //--------------------------------------------------------------------------------------------------------------

        System.out.println("\n\n¡¡¡ LOOKING AT THE HUGIN INTERFACE BOTH huginNetworkFromAMIDST.net AND " +
                "huginNetworkFromAMIDST2.net ARE EXACTLY THE SAME !!!");

        //--------------------------------------------------------------------------------------------------------------

    }

    @Test
    public void testHuginAndAmidstModels() throws ExceptionHugin {

        this.testNumberOfVariables();
        int numVars = amidstBN.getNumberOfVars();

        for (int i = 0; i < numVars; i++) {

            Variable amidstVar = amidstBN.getStaticVariables().getVariableById(i);
            Node huginVar = (Node) huginBN.getNodes().get(i);
            this.testName(amidstVar,huginVar);
            this.testVariableType(amidstVar,huginVar);
            this.testParents(amidstVar, huginVar);
            this.testConditionalDistribution(amidstVar,huginVar);
        }
    }

    private void testNumberOfVariables() throws ExceptionHugin {
        assertEquals(amidstBN.getNumberOfVars(), huginBN.getNodes().size());
    }

    private void testName(Variable amidstVar, Node huginVar) throws ExceptionHugin {
        assertEquals(amidstVar.getName(), huginVar.getName());
    }

    private void testVariableType(Variable amidstVar, Node huginVar) throws ExceptionHugin {
        boolean amidstMultinomialVar = amidstVar.isMultinomial();
        boolean amidstNormalVar = amidstVar.isNormal();
        boolean huginMultinomialVar = huginVar.getKind().compareTo(NetworkModel.H_KIND_DISCRETE) == 0;
        boolean huginNormalVar = huginVar.getKind().compareTo(NetworkModel.H_KIND_CONTINUOUS) == 0;

        assertEquals(amidstMultinomialVar, huginMultinomialVar);
        assertEquals(amidstNormalVar, huginNormalVar);
    }

    private void testParents(Variable amidstVar, Node huginVar) throws ExceptionHugin {
        ParentSet parentsAmidstVar = amidstBN.getDAG().getParentSet(amidstVar);
        NodeList parentsHuginVar = huginVar.getParents();
        int numParentsAmidstVar = parentsAmidstVar.getNumberOfParents();
        int numParentsHuginVar = parentsHuginVar.size();

        // Number of parents
        assertEquals(numParentsAmidstVar, numParentsHuginVar);


        // Only multinomial parents are indexed in reverse order in Hugin
        //-----------------------------------------------------------------------------
        ArrayList<Integer> multinomialParentsIndexes = new ArrayList();
        for (int j=0;j<parentsHuginVar.size();j++) {
            Node huginParent = parentsHuginVar.get(j);
            if (huginParent.getKind().compareTo(NetworkModel.H_KIND_DISCRETE) == 0) {
                multinomialParentsIndexes.add(j);
            }
        }
        Collections.reverse(multinomialParentsIndexes);
        ArrayList<Integer> parentsIndexes = new ArrayList();
        for (int j=0;j<parentsHuginVar.size();j++) {
            Node huginParent = parentsHuginVar.get(j);
            if (huginParent.getKind().compareTo(NetworkModel.H_KIND_DISCRETE) == 0) {
                parentsIndexes.add(multinomialParentsIndexes.get(0));
                multinomialParentsIndexes.remove(0);
            }
            else {
                parentsIndexes.add(j);
            }
        }
        //------------------------------------------------------------------------------


        for (int j = 0; j < numParentsAmidstVar; j++) {
            Variable parentAmidstVar = parentsAmidstVar.getParents().get(j);
            String parentNameHuginVar = ((Node) parentsHuginVar.get(parentsIndexes.get(j))).getName();
            String parentNameAmidstVar = parentAmidstVar.getName();
            assertEquals(parentNameAmidstVar, parentNameHuginVar);
        }
    }

    private void testConditionalDistribution(Variable amidstVar, Node huginVar) throws ExceptionHugin {
        int type = Utils.getConditionalDistributionType(amidstVar, amidstBN);

        switch (type) {
            case 0:
                this.testMultinomial_MultinomialParents(huginVar, amidstVar);
                break;
            case 1:
                ConditionalLinearGaussian dist1 = amidstBN.getDistribution(amidstVar);
                this.testNormal_NormalParents(huginVar, dist1, 0);
                break;
            case 2:
                this.testNormal_MultinomialParents (huginVar, amidstVar);
                break;
            case 3:
                this.testNormal_MultinomialNormalParents(huginVar, amidstVar);
                break;
            case 4:
                this.testMultinomial(huginVar, amidstVar);
                break;
            case 5:
                this.testNormal(huginVar, amidstVar);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized DistributionType. ");
        }
    }

    private void testMultinomial(Node huginVar, Variable amidstVar) throws ExceptionHugin{
        Multinomial dist = amidstBN.getDistribution(amidstVar);
        double[] huginProbabilities = huginVar.getTable().getData();

        int nStates = amidstVar.getNumberOfStates();

        double[] amidstProbabilitiesAssignment_j = dist.getProbabilities();
        for (int k = 0; k < nStates; k++) {
            // Probability of the state k for the j assignment of the parents
            assertEquals(amidstProbabilitiesAssignment_j[k], huginProbabilities[k], 0.0);
        }

    }

    private void testMultinomial_MultinomialParents(Node huginVar, Variable amidstVar) throws ExceptionHugin{
        Multinomial_MultinomialParents dist = amidstBN.getDistribution(amidstVar);
        double[] huginProbabilities = huginVar.getTable().getData();
        List<Multinomial> probabilities = dist.getMultinomialDistributions();

        int nStates = amidstVar.getNumberOfStates();
        int numParentAssignments =
                MultinomialIndex.getNumberOfPossibleAssignments(dist.getConditioningVariables());

        for (int j = 0; j < numParentAssignments; j++) {
            double[] amidstProbabilitiesAssignment_j = probabilities.get(j).getProbabilities();
            for (int k = 0; k < nStates; k++) {
                // Probability of the state k for the j assignment of the parents
                assertEquals(amidstProbabilitiesAssignment_j[k], huginProbabilities[j * nStates + k], 0.0);
            }
        }
    }

    private void testNormal_NormalParents(Node huginVar, ConditionalLinearGaussian dist1, int assign_j) throws ExceptionHugin {


        // Intercept
        double interceptHugin = ((ContinuousChanceNode)huginVar).getAlpha(assign_j);
        double interceptAmidst = dist1.getBeta0();
        assertEquals(interceptHugin,interceptAmidst ,0.0);

        // Parents coefficients
        List<Variable> normalParents = dist1.getConditioningVariables();
        int numNormalParents = normalParents.size();

        for(int i=0;i<numNormalParents;i++) {
            ContinuousChanceNode huginParent =
                    (ContinuousChanceNode)this.huginBN.getNodeByName(normalParents.get(i).getName());
            double coeff_iHugin = ((ContinuousChanceNode)huginVar).getBeta(huginParent,assign_j);
            double coeff_iAmidst = dist1.getBetaForParent(normalParents.get(i));
            assertEquals(coeff_iHugin,coeff_iAmidst,0);
        }

        // Variance
        double varianceAmidst = Math.pow(dist1.getSd(),2);
        double varianceHugin = ((ContinuousChanceNode)huginVar).getGamma(assign_j);
        assertEquals(varianceAmidst,varianceHugin ,0.0000001);
    }

    private void testNormal (Node huginVar, Variable amidstVar) throws ExceptionHugin  {

        Normal normal = amidstBN.getDistribution(amidstVar);

        double mean_jAmidst = normal.getMean();
        double mean_jHugin = ((ContinuousChanceNode)huginVar).getAlpha(0);
        assertEquals(mean_jAmidst, mean_jHugin,0);

        double variance_jAmidst = Math.pow(normal.getSd(),2);
        double variance_jHugin = ((ContinuousChanceNode)huginVar).getGamma(0);
        assertEquals(variance_jAmidst, variance_jHugin,0.0000001);

    }

    private void testNormal_MultinomialParents (Node huginVar, Variable amidstVar) throws ExceptionHugin  {

        Normal_MultinomialParents dist = amidstBN.getDistribution(amidstVar);
        List<Variable> conditioningVariables = dist.getConditioningVariables();
        int numParentAssignments = MultinomialIndex.getNumberOfPossibleAssignments(conditioningVariables);

        for(int j=0;j<numParentAssignments;j++) {
            Normal normal =  dist.getNormal(j);
            double mean_jAmidst = normal.getMean();
            double mean_jHugin = ((ContinuousChanceNode)huginVar).getAlpha(j);
            assertEquals(mean_jAmidst, mean_jHugin,0);

            double variance_jAmidst = Math.pow(normal.getSd(),2);
            double variance_jHugin = ((ContinuousChanceNode)huginVar).getGamma(j);
            assertEquals(variance_jAmidst, variance_jHugin,0.0000001);
        }
    }

    private void testNormal_MultinomialNormalParents(Node huginVar, Variable amidstVar)throws ExceptionHugin {

        Normal_MultinomialNormalParents dist = amidstBN.getDistribution(amidstVar);

        List<Variable> multinomialParents = dist.getMultinomialParents();
        int numParentAssignments = MultinomialIndex.getNumberOfPossibleAssignments(multinomialParents);

        for(int j=0;j <numParentAssignments;j++) {
            ConditionalLinearGaussian dist1 = dist.getConditionalLinearGaussianDistribution(j);
            this.testNormal_NormalParents(huginVar, dist1 ,j);
        }
    }
}