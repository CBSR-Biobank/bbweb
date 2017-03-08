/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  var panelHeader = 'selectCentre directive header';

  function SuiteMixinFactory(TestSuiteMixin) {

    function SuiteMixin() {
    }

    SuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.createScope = function (options) {
      this.element = angular.element([
        '<select-centre panel-header="{{model.panelHeader}}"',
        '               get-centres="model.getCentres"',
        '               on-centre-selected="model.onCentreSelected"',
        '               limit="model.limit"',
        '               message-no-results="No results match the criteria."',
        '               icon="glyphicon-ok-circle">',
        '</select-centre>'
      ].join(''));
      this.scope = this.$rootScope.$new();
      this.scope.model = _.extend({ panelHeader:  panelHeader }, options);
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('selectCentre');
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

  describe('selectCentreComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var SuiteMixin = new SuiteMixinFactory(TestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);
      this.injectDependencies('$q', '$rootScope', '$compile', 'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/selectCentre/selectCentre.html');
    }));

    it('displays the list of centres', function() {
      var self = this,
          centres = _.map(_.range(20), function () { return self.factory.centre(); }),
          limit = centres.length / 2;

      self.createScope({ getCentres: self.createGetCentresFn(centres), limit: limit });

      expect(self.element.find('li.list-group-item').length).toBe(limit);
      expect(self.element.find('input').length).toBe(1);
    });

    it('displays the pannel header correctly', function() {
      var self = this,
          centres = _.map(_.range(20), function () { return self.factory.centre(); }),
          limit = centres.length / 2;

      self.createScope({ getCentres: self.createGetCentresFn(centres), limit: limit });
      expect(self.element.find('h3').text()).toBe(panelHeader);
    });

    it('has a name filter', function() {
      var centres = [ this.factory.centre() ];
      this.createScope({ getCentres: this.createGetCentresFn(centres), limit: centres.length });
      expect(this.element.find('input').length).toBe(1);
    });


    it('displays pagination controls', function() {
      var self = this,
          centres = _.map(_.range(20), function () { return self.factory.centre(); }),
          limit = centres.length / 2;

      self.createScope({ getCentres: self.createGetCentresFn(centres), limit: limit });
      expect(self.controller.showPagination).toBe(true);
      expect(self.element.find('ul.pagination-sm').length).toBe(1);
    });

    it('updates to name filter cause studies to be re-loaded', function() {
      var self = this,
          centres = _.map(_.range(8), function () { return self.factory.centre(); }),
          limit = centres.length / 2;

      self.createScope({ getCentres: self.createGetCentresFn(centres), limit: limit });
      spyOn(self.scope.model, 'getCentres').and.callThrough();

      _.forEach([
        { callCount: 1, nameFilter: 'test' },
        { callCount: 2, nameFilter: '' }
      ], function (obj) {
        self.controller.nameFilter = obj.nameFilter;
        self.controller.nameFilterUpdated();
        expect(self.scope.model.getCentres.calls.count()).toBe(obj.callCount);
      });
    });

    it('page change causes centres to be re-loaded', function() {
      var self = this,
          centres = _.map(_.range(8), function () { return self.factory.centre(); }),
          limit = centres.length / 2;

      self.createScope({ getCentres: self.createGetCentresFn(centres), limit: limit });
      spyOn(self.scope.model, 'getCentres').and.callThrough();
      self.controller.pageChanged();
      expect(self.scope.model.getCentres).toHaveBeenCalled();
    });

    it('clear filter causes centres to be re-loaded', function() {
      var self = this,
          centres = _.map(_.range(8), function () { return self.factory.centre(); }),
          limit = centres.length / 2;

      self.createScope({ getCentres: self.createGetCentresFn(centres), limit: limit });
      spyOn(self.scope.model, 'getCentres').and.callThrough();
      self.controller.clearFilter();
      expect(self.scope.model.getCentres).toHaveBeenCalled();
    });

    it('studyGlyphicon returns valid image tag', function() {
      var self = this,
          centres = _.map(_.range(8), function () { return self.factory.centre(); }),
          limit = centres.length / 2;

      self.createScope({ getCentres: self.createGetCentresFn(centres), limit: limit });
      expect(self.controller.centreGlyphicon())
        .toEqual('<i class="glyphicon glyphicon-ok-circle"></i>');
    });

  });

});
