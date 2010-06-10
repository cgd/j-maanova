package org.jax.maanova.fit;

/**
 * Options for the fit method. We're not allowing "noest" though.
 * Here's how its documented in R/maanova's fitmaanova function
 * method=c("REML","ML","MINQE-I","MINQE-UI", "noest")
 */
public enum MixedModelSolutionMethod
{
    /**
     * see R/maanova docs
     */
    RESTRICTED_MAXIMUM_LIKLIHOOD
    {
        @Override
        public String getRParameterString()
        {
            return "REML";
        }
        
        @Override
        public String toString()
        {
            return "Restricted Maximum Liklihood (REML)";
        }
    },
    
    /**
     * see R/maanova docs
     */
    MAXIMUM_LIKLIHOOD
    {
        @Override
        public String getRParameterString()
        {
            return "ML";
        }
        
        @Override
        public String toString()
        {
            return "Maximum Liklihood (ML)";
        }
    },
    
//    /**
//     * see R/maanova docs
//     */
//    MINQE_I
//    {
//        @Override
//        public String getRParameterString()
//        {
//            return "MINQE-I";
//        }
//        
//        @Override
//        public String toString()
//        {
//            return "MINQE(I)";
//        }
//    },
//    
//    /**
//     * see R/maanova docs
//     */
//    MINQE_UI
//    {
//        @Override
//        public String getRParameterString()
//        {
//            return "MINQE-UI";
//        }
//        
//        @Override
//        public String toString()
//        {
//            return "MINQE(U, I)";
//        }
//    }
    ;
    
    /**
     * Getter for the parameter string that should be given to R/maanova
     * for the particular method
     * @return
     *          the R/maanova parameter string
     */
    public abstract String getRParameterString();
}