define(['underscore'], function(_) {
  'use strict';

  annotationFactoryFactory.$inject = [
    'funutils',
    'validationService',
    'AnnotationValueType',
    'DateTimeAnnotation',
    'MultipleSelectAnnotation',
    'NumberAnnotation',
    'SingleSelectAnnotation',
    'TextAnnotation'
  ];

  function annotationFactoryFactory(funutils,
                                    validationService,
                                    AnnotationValueType,
                                    DateTimeAnnotation,
                                    MultipleSelectAnnotation,
                                    NumberAnnotation,
                                    SingleSelectAnnotation,
                                    TextAnnotation) {

    var requiredKeys = ['annotationTypeId', 'selectedValues'];

    var validateIsMap = validationService.condition1(
      validationService.validator('must be a map', _.isObject));

    var createObj = funutils.partial1(validateIsMap, _.identity);

    var validateObj = funutils.partial(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, requiredKeys))),
      createObj);

    var validateSelectedValues = funutils.partial(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys('annotationTypeId', 'value'))),
      createObj);

    var service = {
      create: create
    };
    return service;

    //--

    /**
     * These objects are used by HTML form code to manage annotation information. It differs from the
     * server representation in order to make setting the information via an HTML simpler.
     *
     * This static method should be used instead of the constructor when creating an annotation from a server
     * response, since it validates that the required fields are present.
     *
     * @param {Object} obj - the server side entity
     *
     * @param {AnnotationType} annotationType the annotation type this annotation is based from
     *
     * @param {boolean} required set only if annotationType does not have a 'required' attribute.
     */
    function create(obj, annotationType, required) {
      var annotValid,
          validation,
          annotation;

      if (obj) {
        validation = validateObj(obj);

        if (!_.isObject(validation)) {
          throw new Error('invalid object from server: ' + validation);
        }

        annotValid =_.reduce(obj.selectedValues, function (memo, selectedValue) {
          var validation = validateSelectedValues(selectedValue);
          return memo && _.isObject(validation);
        }, true);

        if (!annotValid) {
          throw new Error('invalid selected values in object from server');
        }
      }

      if (_.isUndefined(annotationType)) {
        throw new Error('annotation type is undefined');
      }

      switch (annotationType.valueType) {

      case AnnotationValueType.TEXT():
        annotation = new TextAnnotation(obj, annotationType, required);
        break;

      case AnnotationValueType.NUMBER():
        annotation = new NumberAnnotation(obj, annotationType, required);
        break;

      case AnnotationValueType.DATE_TIME():
        annotation = new DateTimeAnnotation(obj, annotationType, required);
        break;

      case AnnotationValueType.SELECT():
        if (annotationType.isSingleSelect()) {
          annotation = new SingleSelectAnnotation(obj, annotationType, required);
        } else if (annotationType.isMultipleSelect()) {
          annotation = new MultipleSelectAnnotation(obj, annotationType, required);
        } else {
          throw new Error('invalid select annotation: ' + annotationType.maxValueCount);
        }
        break;

      default:
        // should never happen since this is checked for in the constructor, but just in case
        throw new Error('value type is invalid: ' + annotationType.valueType);
      }

      return annotation;
    }
  }

  return annotationFactoryFactory;
});
