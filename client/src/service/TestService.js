import jpaService from "./JPAService";

const testService = {
    findAll: () => jpaService.findAll("tests"),
    findByParent: (test) => jpaService.findByParent("tests","ministry",test),
    createOrUpdate: (test) => {return jpaService.createOrUpdate("tests",test)},
    delete: (id) => jpaService.delete("tests",id),
};

export default testService;

export const testFields = [
    {
        name: 'id',
        label: 'ID',
        type: 'text',
        sortable: true,
        hidden: true,
        editable: false,
        required: false,
        width: '10%'
    },
    {
        name: 'description',
        label: 'Description',
        type: 'text',
        sortable: true,
        hidden: false,
        editable: true,
        required: true,
        width: '40%'
    },
    {
        name: 'size',
        label: 'Size',
        type: 'number',
        sortable: true,
        displayInTable: true,
        hidden: false,
        editable: true,
        required: true,
        width: '20%'
    }
];

export const createEmptyTest = () => ({
    description: '',
    size: null
});

