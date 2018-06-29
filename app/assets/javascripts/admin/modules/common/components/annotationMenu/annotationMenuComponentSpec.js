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

      this.createController = (annotation, allowChanges) => {
        this.view = jasmine.createSpy('view');
        this.update = jasmine.createSpy('update');
        this.remove = jasmine.createSpy('remove');

        this.createControllerInternal(
          `<annotation-type-menu
              annotation-type="vm.annotation"
              allow-changes="${allowChanges}"
              on-view="vm.view"
              on-update="vm.update"
              on-remove="vm.remove">
           </annotation-type-menu>`,
          {
            annotation,
            view:   this.view,
            update: this.update,
            remove: this.remove
          },
          'annotationMenu');
      }
    });
  });

  it('initialization is valid', function() {
    const annotation = this.Factory.annotation();
    this.createController(annotation, true);

    expect(this.controller.annotation).toBe(annotation);
    expect(this.controller.allowChanges).toBe(true);
    expect(this.controller.onView()).toBeFunction();
    expect(this.controller.onUpdate()).toBeFunction();
    expect(this.controller.onRemove()).toBeFunction();
  });

  describe('when allowChages is TRUE', function() {

    it('when allowChages is TRUE only two menu options are available', function() {
      const annotation = this.Factory.annotation();
      this.createController(annotation, true);
      const hyperlinks = this.element.find('a');
      expect(hyperlinks.length).toBe(2);
    });

    it('`onView` is called', function() {
      const annotation = this.Factory.annotation();
      this.createController(annotation, true);
      const hyperlinks = this.element.find('a');

      let updateHyperlink, removeHyperlink;

      expect(hyperlinks.length).toBe(2);
      _.range(hyperlinks.length).forEach(index => {
        const hyperlinkText = hyperlinks.eq(index).text().trim();
        if (hyperlinkText === 'Update this annotation') {
          updateHyperlink = hyperlinks.eq(index);
        }
        if (hyperlinkText === 'Remove this annotation') {
          removeHyperlink = hyperlinks.eq(index);
        }
      });

      expect(updateHyperlink).toBeDefined();
      expect(removeHyperlink).toBeDefined();

      updateHyperlink.click().trigger('change');
      expect(this.update).toHaveBeenCalled();
      removeHyperlink.click().trigger('change');
      expect(this.remove).toHaveBeenCalled();
    });

  });

  describe('when allowChages is FALSE', function() {

    it('only one menu options is available', function() {
      const annotation = this.Factory.annotation();
      this.createController(annotation, false);
      const hyperlinks = this.element.find('a');
      expect(hyperlinks.length).toBe(1);
    });

    it('`onView` is called', function() {
      const annotation = this.Factory.annotation();
      this.createController(annotation, false);
      const hyperlinks = this.element.find('a');

      let viewHyperlink;

      expect(hyperlinks.length).toBe(1);
      _.range(hyperlinks.length).forEach(index => {
        const hyperlinkText = hyperlinks.eq(index).text().trim();
        if (hyperlinkText === 'View this annotation') {
          viewHyperlink = hyperlinks.eq(index);
        }
      });

      expect(viewHyperlink).toBeDefined();
      viewHyperlink.click().trigger('change');
      expect(this.view).toHaveBeenCalled();
    });
  });

});
