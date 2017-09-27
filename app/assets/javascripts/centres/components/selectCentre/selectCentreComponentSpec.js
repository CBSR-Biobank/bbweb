/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  var panelHeader = 'selectCentre directive header';

  describe('selectCentreComponent', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (options) {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<select-centre panel-header="{{vm.panelHeader}}"',
            '               get-centres="vm.getCentres"',
            '               on-centre-selected="vm.onCentreSelected"',
            '               limit="vm.limit"',
            '               message-no-results="No results match the criteria."',
            '               icon="glyphicon-ok-circle">',
            '</select-centre>'
          ].join(''),
          _.extend({ panelHeader:  panelHeader }, options),
          'selectCentre');
      };

      SuiteMixin.prototype.createGetCentresFn = function (centres) {
        var self = this;
        return getCentres;

        function getCentres (pagerOptions) {
          return self.$q.when({
            items:    centres.slice(0, pagerOptions.limit),
            page:     0,
            offset:   0,
            total:    centres.length,
            limit:    pagerOptions.limit,
            maxPages: centres.length / pagerOptions.limit
          });
        }
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q', '$rootScope', '$compile', 'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/selectCentre/selectCentre.html');
    }));

    it('displays the list of centres', function() {
      var self = this,
          centres = _.map(_.range(20), function () { return self.factory.centre(); }),
          limit = centres.length / 2;

      self.createController({ getCentres: self.createGetCentresFn(centres), limit: limit });

      expect(self.element.find('li.list-group-item').length).toBe(limit);
      expect(self.element.find('input').length).toBe(1);
    });

    it('displays the pannel header correctly', function() {
      var self = this,
          centres = _.map(_.range(20), function () { return self.factory.centre(); }),
          limit = centres.length / 2;

      self.createController({ getCentres: self.createGetCentresFn(centres), limit: limit });
      expect(self.element.find('h3').text()).toBe(panelHeader);
    });

    it('has a name filter', function() {
      var centres = [ this.factory.centre() ];
      this.createController({ getCentres: this.createGetCentresFn(centres), limit: centres.length });
      expect(this.element.find('input').length).toBe(1);
    });


    it('displays pagination controls', function() {
      var self = this,
          centres = _.map(_.range(20), function () { return self.factory.centre(); }),
          limit = centres.length / 2;

      self.createController({ getCentres: self.createGetCentresFn(centres), limit: limit });
      expect(self.controller.showPagination).toBe(true);
      expect(self.element.find('ul.pagination-sm').length).toBe(1);
    });

    it('updates to name filter cause centres to be re-loaded', function() {
      var self = this,
          centres = _.map(_.range(8), function () { return self.factory.centre(); }),
          limit = centres.length / 2;

      self.createController({ getCentres: self.createGetCentresFn(centres), limit: limit });
      spyOn(self.controller, 'getCentres').and.callThrough();

      _.forEach([
        { callCount: 1, nameFilter: 'test' },
        { callCount: 2, nameFilter: '' }
      ], function (obj) {
        self.controller.nameFilter = obj.nameFilter;
        self.controller.nameFilterUpdated();
        expect(self.controller.getCentres.calls.count()).toBe(obj.callCount);
      });
    });

    it('page change causes centres to be re-loaded', function() {
      var self = this,
          centres = _.map(_.range(8), function () { return self.factory.centre(); }),
          limit = centres.length / 2;

      self.createController({ getCentres: self.createGetCentresFn(centres), limit: limit });
      spyOn(self.controller, 'getCentres').and.callThrough();
      self.controller.pageChanged();
      expect(self.controller.getCentres).toHaveBeenCalled();
    });

    it('clear filter causes centres to be re-loaded', function() {
      var self = this,
          centres = _.map(_.range(8), function () { return self.factory.centre(); }),
          limit = centres.length / 2;

      self.createController({ getCentres: self.createGetCentresFn(centres), limit: limit });
      spyOn(self.controller, 'getCentres').and.callThrough();
      self.controller.clearFilter();
      expect(self.controller.getCentres).toHaveBeenCalled();
    });

    it('studyGlyphicon returns valid image tag', function() {
      var self = this,
          centres = _.map(_.range(8), function () { return self.factory.centre(); }),
          limit = centres.length / 2;

      self.createController({ getCentres: self.createGetCentresFn(centres), limit: limit });
      expect(self.controller.centreGlyphicon())
        .toEqual('<i class="glyphicon glyphicon-ok-circle"></i>');
    });

  });

});
