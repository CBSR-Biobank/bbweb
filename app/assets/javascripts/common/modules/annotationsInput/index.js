/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import DomainModule from '../../../domain'
import _            from 'lodash'
import angular      from 'angular'

/**
 * Creates a module with one main directives and child directives.
 */
const ngModule = angular.module('biobank.annotationsInput', [ DomainModule ])
export default ngModule.name

ngModule.directive('annotationsInput', annotationsInputDirective)

const annotations = [
  'text',
  'number',
  'dateTime',
  'singleSelect',
  'multipleSelect'
]

init()

/**
 * Creates the child directives.
 */
function init() {
  annotations.forEach((annotation) => {
    var name = annotation + 'Annotation',
        directive = directiveGenerator(name)
    ngModule.directive(name, directive)
  })
}

function directiveGenerator(name) {
  return function () {
    var directive = {
      restrict: 'E',
      template : require('./' + name + '.html')
    }

    if (name === 'dateTimeAnnotation') {
      _.extend(directive,
               {
                 bindToController: { annotation: '=' },
                 controller: DateTimeController,
                 controllerAs: 'vm'
               })
    }

    return directive
  }

  function DateTimeController() {
    var vm = this

    vm.dateTimeOnEdit = dateTimeOnEdit

    function dateTimeOnEdit(datetime) {
      vm.annotation.value = datetime
    }
  }
}

class AnnotationsInputCtrl {
}

/**
 * Annotations must contain an attribute named 'annotationType' with the annotation type's information.
 */
function annotationsInputDirective() {
  var directive = {
    restrict: 'E',
    scope: {},
    bindToController: {
      annotations: '='
    },
    template : require('./annotationsInput.html'),
    controller: AnnotationsInputCtrl,
    controllerAs: 'vm'
  }

  return directive
}
