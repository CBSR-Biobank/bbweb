/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import _ from 'lodash'
import ngModule from '../../index'

describe('tagsListComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin)

      this.injectDependencies('Factory')

      this.createController =
        (tagData = this.tagData, tagClass = undefined, onTagSelected = this.tagSelected) => {
          const tagClassAttr = tagClass ? `tag-class="${tagClass}"` : ''

          ComponentTestSuiteMixin.createController.call(
            this,
            `<tags-list tag-data="vm.tagData"
                       ${tagClassAttr}
                       on-tag-selected="vm.onTagSelected">
             </tags-input>`,
            {
              tagData,
              onTagSelected
            },
            'tagsList')
        }

      this.createTagInfo = (count) =>
        _.range(count).map(() => ({
          label:   this.Factory.stringNext(),
          tooltip: this.Factory.stringNext(),
          obj:     { info: Symbol(this.Factory.stringNext()) }
        }))

      this.tagData = this.createTagInfo(1)
      this.tagSelected = jasmine.createSpy('tagSelected').and.returnValue(null)
    })
  })

  it('has valid scope', function() {
    const tagClass = this.Factory.stringNext()
    this.createController(this.tagData, tagClass, this.tagSelected)
    expect(this.controller.tagData).toBeArrayOfSize(this.tagData.length)
    expect(this.controller.tagData[0]).toBe(this.tagData[0])
    expect(this.controller.tagClass).toBe(tagClass)
    expect(this.controller.onTagSelected).toBeFunction()
  })

  it('if not tag class is specified, then default one is used', function() {
    this.createController()
    expect(this.controller.tagClass).toBe('label-info')
  })

  describe('for tagSelected', function() {

    it('onTagSelected is called', function() {
      this.createController()
      this.controller.tagSelected(this.tagData[0])
      expect(this.tagSelected).toHaveBeenCalledWith(this.tagData[0].obj)
    })

    it('onTagSelected can be null', function() {
      this.createController(this.tagData, undefined, null);
      this.controller.tagSelected(this.tagData[0]);
      expect(this.tagSelected).not.toHaveBeenCalled();
    })

  })

})
