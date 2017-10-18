/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * Used to update the value of an annotation.
 *
 * A modal is presented the user where the value can be changed.
 *
 * @return {domain.annotations.Annotation} The annotation with the new value.
 */
/* @ngInject */
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
    if (!title) {
      title = 'Update ' + annotation.annotationType.name;
    }

    if (annotation instanceof DateTimeAnnotation) {
      return modalInput.dateTime(title,
                                 annotation.annotationType.name,
                                 annotation.stringValue,
                                 { required: annotation.required }
                                ).result.then(assignNewValue);
    } else if (annotation instanceof MultipleSelectAnnotation) {
      return modalInput.selectMultiple(title,
                                       annotation.annotationType.name,
                                       annotation.getValue(),
                                       {
                                         required: annotation.required,
                                         selectOptions: annotation.annotationType.options
                                       })
        .result.then(assignNewValue);
    } else if (annotation instanceof NumberAnnotation) {
      return modalInput.number(title,
                               annotation.annotationType.name,
                               annotation.getValue(),
                               { required: annotation.required }
                              ).result.then(assignNewValue);
    } else if (annotation instanceof SingleSelectAnnotation) {
      return modalInput.select(title,
                               annotation.annotationType.name,
                               annotation.getValue(),
                               {
                                 required: annotation.required,
                                 selectOptions: annotation.annotationType.options
                               }
                              ).result.then(assignNewValue);
    } else if (annotation instanceof TextAnnotation) {
      return modalInput.text(title,
                             annotation.annotationType.name,
                             annotation.stringValue,
                             { required: annotation.required }
                            ).result.then(assignNewValue);
    } else {
      throw new Error('invalid annotation type: ' + annotation);
    }

    function assignNewValue(newValue) {
      annotation.setValue(newValue);
      return annotation;
    }
  }

}

export default ngModule => ngModule.service('annotationUpdate', annotationUpdateService)
