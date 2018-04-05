/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * Used to update the value of an {@link domain.annotations.Annotation Annotation}.
 *
 * @memberOf common.services
 */
class AnnotationUpdateService {

  constructor(modalInput,
              DateTimeAnnotation,
              MultipleSelectAnnotation,
              NumberAnnotation,
              SingleSelectAnnotation,
              TextAnnotation) {
    'ngInject';
    Object.assign(this,
                  {
                    modalInput,
                    DateTimeAnnotation,
                    MultipleSelectAnnotation,
                    NumberAnnotation,
                    SingleSelectAnnotation,
                    TextAnnotation
                  });
  }

  /**
   * Opens a modal where the user an change the value of an {@link domain.annotations.Annotation Annotation}.
   *
   * @param {domain.annotations.Annotation} annotation - the annotation to update.
   *
   * @param {string} title - the text to display in the modal's title area.
   *
   * @return {domain.annotations.Annotation} The annotation with the new value.
   */
  update(annotation, title) {
    if (!title) {
      title = 'Update ' + annotation.annotationType.name;
    }

    if (annotation instanceof this.DateTimeAnnotation) {
      return this.modalInput.dateTime(title,
                                      annotation.annotationType.name,
                                      annotation.stringValue,
                                      { required: annotation.required }
                                     ).result.then(assignNewValue);
    } else if (annotation instanceof this.MultipleSelectAnnotation) {
      return this.modalInput.selectMultiple(title,
                                            annotation.annotationType.name,
                                            annotation.getValue(),
                                            {
                                              required: annotation.required,
                                              selectOptions: annotation.annotationType.options
                                            })
        .result.then(assignNewValue);
    } else if (annotation instanceof this.NumberAnnotation) {
      return this.modalInput.number(title,
                                    annotation.annotationType.name,
                                    annotation.getValue(),
                                    { required: annotation.required }
                                   ).result.then(assignNewValue);
    } else if (annotation instanceof this.SingleSelectAnnotation) {
      return this.modalInput.select(title,
                                    annotation.annotationType.name,
                                    annotation.getValue(),
                                    {
                                      required: annotation.required,
                                      selectOptions: annotation.annotationType.options
                                    }
                                   ).result.then(assignNewValue);
    } else if (annotation instanceof this.TextAnnotation) {
      return this.modalInput.text(title,
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

export default ngModule => ngModule.service('annotationUpdate', AnnotationUpdateService)
