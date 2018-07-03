/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import _ from 'lodash';
import ngModule from '../../index';

describe('annotationMenuComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('Factory');

      this.createController = (annotation) => {
        this.view = jasmine.createSpy('view');
        this.update = jasmine.createSpy('update');
        this.remove = jasmine.createSpy('remove');

        this.createControllerInternal(
          `<annotation-menu
              annotation="vm.annotation"
              on-update="vm.update">
           </annotation-menu>`,
          {
            annotation,
            update: this.update
          },
          'annotationMenu');
      }
    });
  });

  it('initialization is valid', function() {
    const annotation = this.Factory.annotation();
    this.createController(annotation);

    expect(this.controller.annotation).toBe(annotation);
    expect(this.controller.onUpdate()).toBeFunction();
  });

  it('only one menu options is available', function() {
    const annotation = this.Factory.annotation();
    this.createController(annotation);
    const hyperlinks = this.element.find('a');
    expect(hyperlinks.length).toBe(1);
  });

});
