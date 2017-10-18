/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

const AnnotationMaxValueCount = {
  NONE: 0,
  SELECT_SINGLE: 1,
  SELECT_MULTIPLE: 2
};

export default ngModule => ngModule.constant('AnnotationMaxValueCount', AnnotationMaxValueCount)
