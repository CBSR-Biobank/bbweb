/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import DomainModule from '../../../domain'
import angular      from 'angular'

/**
 * AngularJS Module related to {@link domain.annotations.Annotation Annotations}.
 * @namespace common.modules.annotationsInput
 */

/**
 * Creates a module with one main component, {@link common.modules.annotationsInput.annotationsInputComponent
 * annotationsInputComponent}, and various child components that are customized for the different {@link
 * domain.annotations.AnnotationValueType AnnotationValueTypes} an {@link domain.annotations.Annotation Annotation} can have.
 *
 * @memberOf common.modules.annotationsInput
 */
const ngAnnotationsInputModule = angular.module('biobank.annotationsInput', [ DomainModule ])

class AnnotationsInputController {}

/**
 * An AngularJS Component that can be used in an HTML form to input information for an array of {@link
 * domain.annotations.Annotation Annotations}.
 *
 * The fields that are displayed in the form depend on the {@link domain.annotations.Annotation Annotation's} `valueType`.
 *
 * @memberOf common.modules.annotationsInput
 *
 * @param {Array<domain.annotations.Annotation>} Annotations - the annotations to display. They must contain an attribute
 * named `annotationType` with the {@link domain.annotations.AnnotationType AnnotationType's} information.
 */
const annotationsInputComponent = {
  bindings: {
    annotations: '<'
  },
  template: require('./annotationsInput.html'),
  controller: AnnotationsInputController,
  controllerAs: 'vm'
};

const annotations = [
  'text',
  'number',
  'dateTime',
  'singleSelect',
  'multipleSelect'
]

class AnnotationController {

  dateTimeOnEdit(datetime) {
    this.annotation.value = datetime
  }
}

/*
 * Creates the child directives.
 */
function init() {
  ngAnnotationsInputModule.component('annotationsInput', annotationsInputComponent);
  annotations.forEach((annotation) => {
    const name = annotation + 'Annotation',
          component = componentGenerator(name)
    ngAnnotationsInputModule.component(name, component)
  })
}

function componentGenerator(name) {
  const component = {
    template : require('./' + name + '.html'),
    controller: AnnotationController,
    controllerAs: 'vm',
    bindings: {
      annotation: '='
    }
  }

  return component;
}

init()

export default ngAnnotationsInputModule.name
