/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
class ShipmentsTableController {

  constructor($state,
              Shipment,
              ShipmentState,
              SHIPMENT_TYPES,
              timeService) {
    'ngInject';

    Object.assign(this, {
      $state,
      Shipment,
      ShipmentState,
      SHIPMENT_TYPES,
      timeService
    });

    this.onGetTableData = this.getTableData.bind(this);
  }

  $onInit() {
    this.statesToDisplay  = [];
    this.tableDataLoading = true;
    this.limit            = 5;
    this.shipmentDates    = {};
    this.tableController  = null;
    this.centreFilter     = null;

    switch (this.shipmentTypes) {
    case this.SHIPMENT_TYPES.INCOMING:
      this.centreFilter = `toCentre:in:(${this.centre.name})`;
      break;
    case this.SHIPMENT_TYPES.OUTGOING:
      this.centreFilter = `fromCentre:in:(${this.centre.name})`;
      break;
    case this.SHIPMENT_TYPES.COMPLETED:
      this.centreFilter = `withCentre:in:(${this.centre.name})`;
      break;
    default:
      throw new Error('shipmentTypes is invalid: ' + this.shipmentTypes);
    }

    if (this.shipmentTypes === this.SHIPMENT_TYPES.COMPLETED) {
      this.statesToDisplay = [ this.ShipmentState.COMPLETED ];
    } else {
      this.statesToDisplay = [
        this.ShipmentState.CREATED,
        this.ShipmentState.PACKED,
        this.ShipmentState.SENT,
        this.ShipmentState.RECEIVED,
        this.ShipmentState.UNPACKED,
        this.ShipmentState.LOST
      ];
    }

    this.states = [{ label: 'any',  value: '' }].concat(
      this.statesToDisplay.map(function (state) {
        return { label: state, value: state };
      }));
    this.stateFilter = this.states[0].value;
  }

  getTableData(tableState, controller) {
    var pagination            = tableState.pagination,
        searchPredicateObject = tableState.search.predicateObject || {},
        sortPredicate         = tableState.sort.predicate || 'courierName',
        sortOrder             = tableState.sort.reverse || false,
        filters               = [],
        options = {
          sort:  sortPredicate,
          page:  1 + (pagination.start / this.limit),
          limit: this.limit
        };

    if (!this.tableController && controller) {
      this.tableController = controller;
    }

    if (this.centreFilter) {
      filters.push(this.centreFilter);
    }

    if (searchPredicateObject.courierName) {
      filters.push('courierName:like:' + searchPredicateObject.courierName);
    }

    if (searchPredicateObject.trackingNumber) {
      filters.push('trackingNumber:like:' + searchPredicateObject.trackingNumber);
    }

    if (this.stateFilter === '') {
      filters.push('state:in:(' + this.statesToDisplay.join(',') + ')');
    } else {
      filters.push('state:in:(' + this.stateFilter + ')');
    }

    if (filters.length > 0) {
      options.filter = filters.join(';');
    }

    if (sortOrder) {
      options.sort = '-' + options.sort;
    }

    this.tableDataLoading = true;
    this.Shipment.list(options).then((paginationResult) => {
      this.shipments = paginationResult.items;
      this.hasShipments = (this.shipments.length > 0);
      tableState.pagination.numberOfPages = paginationResult.maxPages;
      this.tableDataLoading = false;

      this.shipmentDates = {};
      this.shipments.forEach((shipment) => {
        if (shipment.isCreated()) {
          this.shipmentDates[shipment.id] = this.timeService.dateToDisplayString(shipment.timeAdded);
        } else {
          this.shipmentDates[shipment.id] = this.timeService.dateToDisplayString(shipment.timePacked);
        }
      });
    });
  }

  shipmentInformation(shipment) {
    if (shipment.state === this.ShipmentState.CREATED) {
      this.$state.go('home.shipping.addItems', { shipmentId: shipment.id });
    } else if (shipment.state === this.ShipmentState.UNPACKED) {
      this.$state.go('home.shipping.shipment.unpack.info', { shipmentId: shipment.id });
    } else {
      this.$state.go('home.shipping.shipment', { shipmentId: shipment.id });
    }
  }
}

/**
 * Displays the shipments originating from, or destined to, a centre.
 *
 * @param {domain.centres.Centre} centre - The centre to display shipments for.
 *
 * @param {domain.centres.ShipmentTypes} shipmentTypes - the type of shipments to display.
 */
const COMPONENT = {
  template: require('./shipmentsTable.html'),
  controller: ShipmentsTableController,
  controllerAs: 'vm',
  bindings: {
    centre:        '<',
    shipmentTypes: '@'
  }
};

export default ngModule => ngModule.component('shipmentsTableComponent',  COMPONENT)
