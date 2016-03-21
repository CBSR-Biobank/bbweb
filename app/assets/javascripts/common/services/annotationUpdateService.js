/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  annotationUpdateService.$inject = [
    'modalInput',
    'DateTimeAnnotation',
    'MultipleSelectAnnotation',
    'NumberAnnotation',
    'SingleSelectAnnotation',
    'TextAnnotation'
  ];

  /**
   * Used to update the value of an annotation.
   *
   * A modal is presented the user where the value can be changed.
   */
  function annotationUpdateService(modalInput,
                                   DateTimeAnnotation,
                                   MultipleSelectAnnotation,
                                   NumberAnnotation,
                                   SingleSelectAnnotation,
                                   TextAnnotation) {
    var service = {
      update: update
    };


    return service;

    //-------

    function update(annotation, title) {
      if (annotation instanceof DateTimeAnnotation) {
        return modalInput.dateTime(title,
                                   annotation.annotationType.name,
                                   annotation.stringValue,
                                   { required: annotation.required }
        ).then(assignNewValue);
      } else if (annotation instanceof MultipleSelectAnnotation) {
        return modalInput.selectMultiple(title,
                                         annotation.annotationType.name,
                                         annotation.selectedValues,
                                         {
                                           required: annotation.required,
                                           selectOptions: annotation.annotationType.options
                                         })
          .then(assignNewValue);
      } else if (annotation instanceof NumberAnnotation) {
        return modalInput.number(title,
                                 annotation.annotationType.name,
                                 annotation.numberValue,
                                 { required: annotation.required }
        ).then(assignNewValue);
      } else if (annotation instanceof SingleSelectAnnotation) {
        return modalInput.select(title,
                                 annotation.annotationType.name,
                                 annotation.selectedValues[0],
                                 {
                                   required: annotation.required,
                                   selectOptions: annotation.annotationType.options
                                 }
        ).then(assignNewValue);
      } else if (annotation instanceof TextAnnotation) {
        return modalInput.text(title,
                               annotation.annotationType.name,
                               annotation.stringValue,
                               { required: annotation.required }
        ).then(assignNewValue);
      } else {
        throw new Error('invalid annotation type: ' + annotation);
      }

      function assignNewValue(newValue) {
        annotation.setValue(newValue);
        return annotation;
      }
    }

  }

  return annotationUpdateService;
});
